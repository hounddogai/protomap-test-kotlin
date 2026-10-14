package com.acme.transport.safety.coaching.tier

import com.acme.transport.kit.flags.Registry
import com.acme.transport.safety.clock.Clock

/** Moves coaching ledgers through their life cycle and keeps a journal of what it did. */
data class CoachingLedgerCoordinator(
    var store: CoachingLedgerStore,
    var validator: CoachingLedgerValidator,
    var clock: Clock,
    var journal: MutableList<String>,
    var engine: CoachingLedgerEngine,
) {
    private fun note(message: String) {
        journal.add("${clock.now()} $message")
    }

    fun open(code: String) {
        val item = store.find(code) ?: error("coaching ledger $code not found")
        validator.requireValid(item)
        item.moveTo(CoachingLedgerStatus.GRANTED)
        store.save(item)
        note("$code is granted")
    }

    fun close(code: String, reason: String) {
        val item = store.find(code) ?: error("coaching ledger $code not found")
        if (item.isTerminal()) {
            note("$code already closed")
            return
        }
        item.moveTo(CoachingLedgerStatus.EXPIRED)
        store.save(item)
        note("$code is expired: $reason")
    }

    fun score(code: String): Double {
        val item = store.find(code) ?: error("coaching ledger $code not found")
        val result = engine.run(item)
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

fun newCoachingLedgerCoordinator(store: CoachingLedgerStore, clock: Clock, flags: Registry): CoachingLedgerCoordinator {
    return CoachingLedgerCoordinator(
        store = store,
        validator = defaultCoachingLedgerValidator(),
        clock = clock,
        journal = mutableListOf<String>(),
        engine = defaultCoachingLedgerEngine(flags),
    )
}
