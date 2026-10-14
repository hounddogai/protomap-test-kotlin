package com.acme.transport.customs.clearance.quota

import com.acme.transport.customs.clock.Clock
import com.acme.transport.kit.flags.Registry

/** Moves adaptive clearance budgets through their life cycle and keeps a journal of what it did. */
data class AdaptiveClearanceBudgetCoordinator(
    var store: AdaptiveClearanceBudgetStore,
    var validator: AdaptiveClearanceBudgetValidator,
    var clock: Clock,
    var journal: MutableList<String>,
    var engine: AdaptiveClearanceBudgetEngine,
) {
    private fun note(message: String) {
        journal.add("${clock.now()} $message")
    }

    fun open(code: String) {
        val item = store.find(code) ?: error("adaptive clearance budget $code not found")
        validator.requireValid(item)
        item.moveTo(AdaptiveClearanceBudgetStatus.SCHEDULED)
        store.save(item)
        note("$code is scheduled")
    }

    fun close(code: String, reason: String) {
        val item = store.find(code) ?: error("adaptive clearance budget $code not found")
        if (item.isTerminal()) {
            note("$code already closed")
            return
        }
        item.moveTo(AdaptiveClearanceBudgetStatus.CANCELLED)
        store.save(item)
        note("$code is cancelled: $reason")
    }

    fun estimate(code: String): Double {
        val item = store.find(code) ?: error("adaptive clearance budget $code not found")
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

fun newAdaptiveClearanceBudgetCoordinator(store: AdaptiveClearanceBudgetStore, clock: Clock, flags: Registry): AdaptiveClearanceBudgetCoordinator {
    return AdaptiveClearanceBudgetCoordinator(
        store = store,
        validator = defaultAdaptiveClearanceBudgetValidator(),
        clock = clock,
        journal = mutableListOf<String>(),
        engine = defaultAdaptiveClearanceBudgetEngine(flags),
    )
}
