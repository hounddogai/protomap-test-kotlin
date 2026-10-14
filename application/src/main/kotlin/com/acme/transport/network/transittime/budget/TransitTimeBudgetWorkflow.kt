package com.acme.transport.network.transittime.budget

import com.acme.transport.kit.flags.Registry
import com.acme.transport.network.clock.Clock

/** Moves transit time budgets through their life cycle and keeps a journal of what it did. */
data class TransitTimeBudgetManager(
    var store: TransitTimeBudgetStore,
    var validator: TransitTimeBudgetValidator,
    var clock: Clock,
    var journal: MutableList<String>,
    var engine: TransitTimeBudgetEngine,
) {
    private fun note(message: String) {
        journal.add("${clock.now()} $message")
    }

    fun advance(code: String) {
        val item = store.find(code) ?: error("transit time budget $code not found")
        validator.requireValid(item)
        item.moveTo(TransitTimeBudgetStatus.PENDING)
        store.save(item)
        note("$code is pending")
    }

    fun retire(code: String, reason: String) {
        val item = store.find(code) ?: error("transit time budget $code not found")
        if (item.isTerminal()) {
            note("$code already closed")
            return
        }
        item.moveTo(TransitTimeBudgetStatus.CLOSED)
        store.save(item)
        note("$code is closed: $reason")
    }

    fun evaluate(code: String): Double {
        val item = store.find(code) ?: error("transit time budget $code not found")
        val result = engine.compute(item)
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

fun newTransitTimeBudgetManager(store: TransitTimeBudgetStore, clock: Clock, flags: Registry): TransitTimeBudgetManager {
    return TransitTimeBudgetManager(
        store = store,
        validator = defaultTransitTimeBudgetValidator(),
        clock = clock,
        journal = mutableListOf<String>(),
        engine = defaultTransitTimeBudgetEngine(flags),
    )
}
