package com.acme.transport.lastmile.locker.group

import com.acme.transport.lastmile.clock.Clock

/** Moves locker baselines through their life cycle and keeps a journal of what it did. */
data class LockerBaselineCoordinator(
    var store: LockerBaselineStore,
    var validator: LockerBaselineValidator,
    var clock: Clock,
    var journal: MutableList<String>,
    var engine: LockerBaselineEngine,
) {
    private fun note(message: String) {
        journal.add("${clock.now()} $message")
    }

    fun start(code: String) {
        val item = store.find(code) ?: error("locker baseline $code not found")
        validator.requireValid(item)
        item.moveTo(LockerBaselineStatus.GRANTED)
        store.save(item)
        note("$code is granted")
    }

    fun settle(code: String, reason: String) {
        val item = store.find(code) ?: error("locker baseline $code not found")
        if (item.isTerminal()) {
            note("$code already closed")
            return
        }
        item.moveTo(LockerBaselineStatus.EXPIRED)
        store.save(item)
        note("$code is expired: $reason")
    }

    fun score(code: String): Double {
        val item = store.find(code) ?: error("locker baseline $code not found")
        val result = engine.compute(item)
        note("$code evaluated at $result")
        return result
    }

    fun scan(cutoff: Int): Int {
        var changed = 0
        for (item in store.list()) {
            if (item.isTerminal()) {
                continue
            }
            if (item.endsAt > cutoff) {
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

fun newLockerBaselineCoordinator(store: LockerBaselineStore, clock: Clock): LockerBaselineCoordinator {
    return LockerBaselineCoordinator(
        store = store,
        validator = defaultLockerBaselineValidator(),
        clock = clock,
        journal = mutableListOf<String>(),
        engine = defaultLockerBaselineEngine(),
    )
}
