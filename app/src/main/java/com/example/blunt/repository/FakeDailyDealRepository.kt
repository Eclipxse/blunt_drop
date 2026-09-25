package com.example.blunt.repository

import com.example.blunt.data.*
import com.example.blunt.model.*
import java.util.UUID

class FakeDailyDealRepository(private val store: LocalStore) : DailyDealRepository {
    override val state = store.state
    override suspend fun claim() = store.transaction { snapshot ->
        val account = snapshot.requireAccount()
        val deal = snapshot.deal
        require(deal.phase(store.now()) == DealPhase.LIVE) { "This Daily Drop is no longer available. Only one successful claim is allowed." }
        val updated = deal.copy(claimedBy = account.user.id, reservedUntil = store.now() + 600_000)
        val notice = Notice("reservation-${deal.id}", "Your Daily Drop is reserved", "You have 10 minutes to complete the ₹99 demo checkout. Your reservation ends when the timer reaches zero.", "daily")
        snapshot.withAccount(account.copy(notifications = listOf(notice) + account.notifications)).copy(deal = updated) to Unit
    }
    override suspend fun remind() = store.transaction { snapshot ->
        val account = snapshot.requireAccount()
        snapshot.withAccount(account.copy(reminder = !account.reminder)) to Unit
    }
    override suspend fun activateDemo() = store.transaction { snapshot ->
        require(snapshot.deal.claimedBy == null) { "This drop was already claimed. Start a new demo drop from Profile." }
        val notices = snapshot.accounts.map { account -> if (account.reminder) account.copy(notifications = listOf(Notice("live-${snapshot.deal.id}", "The ₹99 Daily Drop is live", "Your reminder is here. One promotional unit is ready to claim.", "daily")) + account.notifications) else account }
        snapshot.copy(accounts = notices, deal = snapshot.deal.copy(startsAt = store.now() - 1, endsAt = store.now() + 86_400_000)) to Unit
    }
    override suspend fun resetDemo() = store.transaction { snapshot ->
        snapshot.copy(deal = DealState(id = UUID.randomUUID().toString(), startsAt = store.now() + 120_000, endsAt = store.now() + 86_400_000)) to Unit
    }
    override suspend fun refreshClock() {
        fun updates(snapshot: AppSnapshot): List<LocalAccount> = snapshot.accounts.map { account ->
            val deal = snapshot.deal
            val current = store.now()
            val notice = when {
                account.reminder && deal.phase(current) == DealPhase.LIVE -> Notice("live-${deal.id}", "The ₹99 Daily Drop is live", "Your reminder is here. One promotional unit is ready to claim.", "daily")
                deal.claimedBy == account.user.id && deal.phase(current) == DealPhase.RESERVED && deal.reservedUntil - current <= 120_000 -> Notice("expiring-${deal.id}", "Your Daily Drop reservation expires soon", "Less than two minutes left. Complete mock checkout before your reservation closes.", "daily")
                else -> null
            }
            if (notice == null || account.notifications.any { it.id == notice.id }) account else account.copy(notifications = listOf(notice) + account.notifications)
        }
        val snapshot = store.state.value
        if (updates(snapshot) == snapshot.accounts) return
        store.transaction { latest -> latest.copy(accounts = updates(latest)) to Unit }
    }
}
