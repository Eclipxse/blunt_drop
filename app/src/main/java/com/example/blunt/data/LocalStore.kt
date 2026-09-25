package com.example.blunt.data

import android.content.Context
import android.util.AtomicFile
import com.example.blunt.model.*
import com.google.gson.Gson
import kotlinx.coroutines.*
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import java.io.File
import java.security.SecureRandom
import javax.crypto.SecretKeyFactory
import javax.crypto.spec.PBEKeySpec
import java.util.Base64

interface SnapshotStorage { fun read(): AppSnapshot?; fun write(snapshot: AppSnapshot) }
class FileSnapshotStorage(context: Context) : SnapshotStorage {
    private val file = AtomicFile(File(context.filesDir, "blunt-v1.json"))
    private val gson = Gson()
    override fun read(): AppSnapshot? = if (!file.baseFile.exists()) null else file.openRead().bufferedReader().use { gson.fromJson(it, AppSnapshot::class.java) }
    override fun write(snapshot: AppSnapshot) {
        val output = file.startWrite()
        try { output.write(gson.toJson(snapshot).toByteArray(Charsets.UTF_8)); file.finishWrite(output) }
        catch (e: Exception) { file.failWrite(output); throw e }
    }
}
object Passwords {
    fun salt(): String = Base64.getEncoder().encodeToString(ByteArray(16).also { SecureRandom().nextBytes(it) })
    fun hash(password: String, salt: String): String {
        val spec = PBEKeySpec(password.toCharArray(), Base64.getDecoder().decode(salt), 60_000, 256)
        return try { Base64.getEncoder().encodeToString(SecretKeyFactory.getInstance("PBKDF2WithHmacSHA256").generateSecret(spec).encoded) } finally { spec.clearPassword() }
    }
}
class LocalStore(
    private val storage: SnapshotStorage,
    scope: CoroutineScope,
    val now: () -> Long = System::currentTimeMillis
) {
    private val mutex = Mutex()
    private val _state = MutableStateFlow(AppSnapshot())
    val state = _state.asStateFlow()
    private val _ready = MutableStateFlow(false)
    val ready = _ready.asStateFlow()
    private val _error = MutableStateFlow<String?>(null)
    val error = _error.asStateFlow()
    private val loaded = CompletableDeferred<Unit>()
    init { scope.launch { initialize() } }
    suspend fun initialize() = mutex.withLock {
        try {
            val snapshot = withContext(Dispatchers.IO) { storage.read() ?: initialSnapshot(now()).also(storage::write) }
            _state.value = snapshot; _error.value = null; _ready.value = true
            if (!loaded.isCompleted) loaded.complete(Unit)
        } catch (_: Exception) { _error.value = "Your saved data could not be opened. Free some storage, then retry." }
    }
    suspend fun <T> transaction(block: (AppSnapshot) -> Pair<AppSnapshot, T>): T {
        loaded.await()
        return mutex.withLock {
            withContext(Dispatchers.IO) {
                val (next, result) = block(_state.value)
                try { storage.write(next) } catch (_: Exception) { throw IllegalStateException("Could not save this change. Check device storage and try again.") }
                _state.value = next
                result
            }
        }
    }
    companion object {
        fun initialSnapshot(now: Long): AppSnapshot {
            val salt = Passwords.salt()
            val demo = LocalAccount(User("demo", "Alex Morgan", "demo@example.com", "9876543210"), salt, Passwords.hash("password123", salt), notifications = starterNotices())
            return AppSnapshot(accounts = listOf(demo), deal = DealState(startsAt = now + 120_000, endsAt = now + 86_400_000))
        }
        fun starterNotices() = listOf(
            Notice("welcome", "A little more for a little less.", "Discover today's edit of useful, beautiful things. All offers in this app are a local demo.", "home"),
            Notice("drop", "Your next Daily Drop", "One Sony headphone. One ₹99 price. Open the drop to see when it starts.", "daily"),
            Notice("edit", "New discounts, freshly picked", "Explore the latest arrivals across your favorite categories.", "listing/All")
        )
    }
}
fun AppSnapshot.requireAccount(): LocalAccount = account ?: error("Please log in to continue.")
fun AppSnapshot.withAccount(account: LocalAccount): AppSnapshot = copy(accounts = accounts.map { if (it.user.id == account.user.id) account else it })
