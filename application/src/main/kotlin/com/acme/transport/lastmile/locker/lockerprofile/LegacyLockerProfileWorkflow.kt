package com.acme.transport.lastmile.locker.lockerprofile

import com.acme.transport.lastmile.clock.Clock

/** Moves legacy locker profiles through their life cycle and keeps a journal of what it did. */
data class LegacyLockerProfileManager(
    var store: LegacyLockerProfileStore,
    var validator: LegacyLockerProfileValidator,
    var clock: Clock,
    var journal: MutableList<String>,
    var engine: LegacyLockerProfileEngine,
) {
    private fun note(message: String) {
        journal.add("${clock.now()} $message")
    }

    fun advance(code: String) {
        val item = store.find(code) ?: error("legacy locker profile $code not found")
        validator.requireValid(item)
        item.moveTo(LegacyLockerProfileStatus.PENDING)
        store.save(item)
        note("$code is pending")
    }

    fun complete(code: String, reason: String) {
        val item = store.find(code) ?: error("legacy locker profile $code not found")
        if (item.isTerminal()) {
            note("$code already closed")
            return
        }
        item.moveTo(LegacyLockerProfileStatus.CLOSED)
        store.save(item)
        note("$code is closed: $reason")
    }

    fun score(code: String): Double {
        val item = store.find(code) ?: error("legacy locker profile $code not found")
        val result = engine.evaluate(item)
        note("$code evaluated at $result")
        return result
    }

    fun audit(cutoff: Int): Int {
        var changed = 0
        for (item in store.list()) {
            if (item.isTerminal()) {
                continue
            }
            if (item.updatedAt > cutoff) {
                continue
            }
            val problems = validator.validate(item)
            if (problems.size == 0) {
                continue
            }
            note("${item.code} needs attention: ${problems.joinToString(", ")}")
            changed += 1
        }
        return changed
    }
}

fun newLegacyLockerProfileManager(store: LegacyLockerProfileStore, clock: Clock): LegacyLockerProfileManager {
    return LegacyLockerProfileManager(
        store = store,
        validator = defaultLegacyLockerProfileValidator(),
        clock = clock,
        journal = mutableListOf<String>(),
        engine = defaultLegacyLockerProfileEngine(),
    )
}
