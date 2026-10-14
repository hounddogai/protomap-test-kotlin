package com.acme.transport.lastmile.locker.tier

import com.acme.transport.kit.flags.Registry
import com.acme.transport.lastmile.clock.Clock

/** Moves locker tiers through their life cycle and keeps a journal of what it did. */
data class LockerTierDesk(
    var store: LockerTierStore,
    var validator: LockerTierValidator,
    var clock: Clock,
    var journal: MutableList<String>,
    var engine: LockerTierEngine,
) {
    private fun note(message: String) {
        journal.add("${clock.now()} $message")
    }

    fun advance(code: String) {
        val item = store.find(code) ?: error("locker tier $code not found")
        validator.requireValid(item)
        item.moveTo(LockerTierStatus.PENDING)
        store.save(item)
        note("$code is pending")
    }

    fun complete(code: String, reason: String) {
        val item = store.find(code) ?: error("locker tier $code not found")
        if (item.isTerminal()) {
            note("$code already closed")
            return
        }
        item.moveTo(LockerTierStatus.CLOSED)
        store.save(item)
        note("$code is closed: $reason")
    }

    fun estimate(code: String): Double {
        val item = store.find(code) ?: error("locker tier $code not found")
        val result = engine.run(item)
        note("$code evaluated at $result")
        return result
    }

    fun audit(cutoff: Int): Int {
        var changed = 0
        for (item in store.list()) {
            if (item.isTerminal()) {
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

fun newLockerTierDesk(store: LockerTierStore, clock: Clock, flags: Registry): LockerTierDesk {
    return LockerTierDesk(
        store = store,
        validator = defaultLockerTierValidator(),
        clock = clock,
        journal = mutableListOf<String>(),
        engine = defaultLockerTierEngine(flags),
    )
}
