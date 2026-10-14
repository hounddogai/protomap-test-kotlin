package com.acme.transport.lastmile.locker.lockerquota

import com.acme.transport.lastmile.clock.Clock

/** Moves lenient locker overrides through their life cycle and keeps a journal of what it did. */
data class LenientLockerOverrideDesk(
    var store: LenientLockerOverrideStore,
    var validator: LenientLockerOverrideValidator,
    var clock: Clock,
    var journal: MutableList<String>,
    var engine: LenientLockerOverrideEngine,
) {
    private fun note(message: String) {
        journal.add("${clock.now()} $message")
    }

    fun submit(code: String) {
        val item = store.find(code) ?: error("lenient locker override $code not found")
        validator.requireValid(item)
        item.moveTo(LenientLockerOverrideStatus.SCHEDULED)
        store.save(item)
        note("$code is scheduled")
    }

    fun complete(code: String, reason: String) {
        val item = store.find(code) ?: error("lenient locker override $code not found")
        if (item.isTerminal()) {
            note("$code already closed")
            return
        }
        item.moveTo(LenientLockerOverrideStatus.CANCELLED)
        store.save(item)
        note("$code is cancelled: $reason")
    }

    fun quote(code: String): Double {
        val item = store.find(code) ?: error("lenient locker override $code not found")
        val result = engine.evaluate(item)
        note("$code evaluated at $result")
        return result
    }

    fun sweep(cutoff: Int): Int {
        var changed = 0
        for (item in store.list()) {
            if (item.isTerminal()) {
                continue
            }
            if (item.startsAt > cutoff) {
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

fun newLenientLockerOverrideDesk(store: LenientLockerOverrideStore, clock: Clock): LenientLockerOverrideDesk {
    return LenientLockerOverrideDesk(
        store = store,
        validator = defaultLenientLockerOverrideValidator(),
        clock = clock,
        journal = mutableListOf<String>(),
        engine = defaultLenientLockerOverrideEngine(),
    )
}
