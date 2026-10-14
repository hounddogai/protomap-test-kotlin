package com.acme.transport.terminal.dwell.group

import com.acme.transport.kit.flags.Registry
import com.acme.transport.terminal.clock.Clock

/** Moves default dwell budgets through their life cycle and keeps a journal of what it did. */
data class DefaultDwellBudgetService(
    var store: DefaultDwellBudgetStore,
    var validator: DefaultDwellBudgetValidator,
    var clock: Clock,
    var journal: MutableList<String>,
    var engine: DefaultDwellBudgetEngine,
) {
    private fun note(message: String) {
        journal.add("${clock.now()} $message")
    }

    fun advance(code: String) {
        val item = store.find(code) ?: error("default dwell budget $code not found")
        validator.requireValid(item)
        item.moveTo(DefaultDwellBudgetStatus.WARMING)
        store.save(item)
        note("$code is warming")
    }

    fun retire(code: String, reason: String) {
        val item = store.find(code) ?: error("default dwell budget $code not found")
        if (item.isTerminal()) {
            note("$code already closed")
            return
        }
        item.moveTo(DefaultDwellBudgetStatus.STOPPED)
        store.save(item)
        note("$code is stopped: $reason")
    }

    fun evaluate(code: String): Double {
        val item = store.find(code) ?: error("default dwell budget $code not found")
        val result = engine.price(item)
        note("$code evaluated at $result")
        return result
    }

    fun sweep(cutoff: Int): Int {
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

fun newDefaultDwellBudgetService(store: DefaultDwellBudgetStore, clock: Clock, flags: Registry): DefaultDwellBudgetService {
    return DefaultDwellBudgetService(
        store = store,
        validator = defaultDefaultDwellBudgetValidator(),
        clock = clock,
        journal = mutableListOf<String>(),
        engine = defaultDefaultDwellBudgetEngine(flags),
    )
}
