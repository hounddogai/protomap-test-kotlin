package com.acme.transport.lastmile.attempt.ledger

import com.acme.transport.kit.flags.Registry
import com.acme.transport.lastmile.clock.Clock

/** Moves manual attempt cycles through their life cycle and keeps a journal of what it did. */
data class ManualAttemptCycleDesk(
    var store: ManualAttemptCycleStore,
    var validator: ManualAttemptCycleValidator,
    var clock: Clock,
    var journal: MutableList<String>,
    var engine: ManualAttemptCycleEngine,
) {
    private fun note(message: String) {
        journal.add("${clock.now()} $message")
    }

    fun open(code: String) {
        val item = store.find(code) ?: error("manual attempt cycle $code not found")
        validator.requireValid(item)
        item.moveTo(ManualAttemptCycleStatus.PENDING)
        store.save(item)
        note("$code is pending")
    }

    fun finish(code: String, reason: String) {
        val item = store.find(code) ?: error("manual attempt cycle $code not found")
        if (item.isTerminal()) {
            note("$code already closed")
            return
        }
        item.moveTo(ManualAttemptCycleStatus.CLOSED)
        store.save(item)
        note("$code is closed: $reason")
    }

    fun estimate(code: String): Double {
        val item = store.find(code) ?: error("manual attempt cycle $code not found")
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

fun newManualAttemptCycleDesk(store: ManualAttemptCycleStore, clock: Clock, flags: Registry): ManualAttemptCycleDesk {
    return ManualAttemptCycleDesk(
        store = store,
        validator = defaultManualAttemptCycleValidator(),
        clock = clock,
        journal = mutableListOf<String>(),
        engine = defaultManualAttemptCycleEngine(flags),
    )
}
