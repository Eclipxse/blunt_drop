package com.example.blunt.repository

import com.example.blunt.data.*
import com.example.blunt.model.*
import java.security.MessageDigest
import java.util.UUID

object Validation {
    fun email(value: String) = value.matches(Regex("^[A-Za-z0-9.!#%&'*+/=?^_`{|}~-]+@[A-Za-z0-9-]+(?:\\.[A-Za-z0-9-]+)+$"))
    fun phone(value: String) = value.matches(Regex("[6-9][0-9]{9}"))
    fun address(address: Address) {
        require(address.name.trim().length >= 2) { "Enter the recipient's full name." }
        require(phone(address.phone)) { "Enter a valid 10-digit Indian mobile number." }
        require(address.house.isNotBlank() && address.street.isNotBlank() && address.area.isNotBlank()) { "Add the house, street and area so your order can reach you." }
        require(address.city.isNotBlank() && address.state.isNotBlank()) { "Enter your city and state." }
        require(address.pin.matches(Regex("[1-9][0-9]{5}"))) { "Enter a valid 6-digit PIN code." }
    }
}
class FakeAuthRepository(private val store: LocalStore) : AuthRepository {
    override val state = store.state
    override suspend fun login(identity: String, password: String) = store.transaction { snapshot ->
        val account = snapshot.accounts.find { it.user.email.equals(identity.trim(), true) || it.user.phone == identity.trim() }
        require(account != null && MessageDigest.isEqual(Passwords.hash(password, account.salt).toByteArray(), account.passwordHash.toByteArray())) { "Email, phone or password is incorrect. Please try again." }
        snapshot.copy(sessionId = account.user.id) to Unit
    }
    override suspend fun register(name: String, phone: String, email: String, password: String, confirm: String) = store.transaction { snapshot ->
        require(name.trim().length >= 2) { "Enter your full name." }
        require(Validation.phone(phone.trim())) { "Enter a valid 10-digit Indian mobile number." }
        require(Validation.email(email.trim())) { "Enter a valid email address." }
        require(password.length >= 8 && password.any(Char::isDigit) && password.any(Char::isLetter)) { "Use at least 8 characters, including a letter and a number." }
        require(password == confirm) { "Passwords don't match." }
        require(snapshot.accounts.none { it.user.email.equals(email.trim(), true) || it.user.phone == phone.trim() }) { "An account already uses that email or phone. Log in instead." }
        val salt = Passwords.salt()
        val user = User(UUID.randomUUID().toString(), name.trim(), email.trim().lowercase(), phone.trim())
        val account = LocalAccount(user, salt, Passwords.hash(password, salt), notifications = LocalStore.starterNotices())
        snapshot.copy(accounts = snapshot.accounts + account) to Unit
    }
    override suspend fun logout() = store.transaction { it.copy(sessionId = null) to Unit }
    override suspend fun completeOnboarding() = store.transaction { it.copy(onboardingCompleted = true) to Unit }
}
