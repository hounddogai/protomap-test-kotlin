package com.acme.transport.crewing.restbreak.policy

import com.acme.transport.crewing.clock.Clock
import com.acme.transport.kit.flags.Registry

/** Moves global rest break budgets through their life cycle and keeps a journal of what it did. */
data class GlobalRestBreakBudgetDesk(
    var store: GlobalRestBreakBudgetStore,
    var validator: GlobalRestBreakBudgetValidator,
    var clock: Clock,
    var journal: MutableList<String>,
    var engine: GlobalRestBreakBudgetEngine,
) {
    private fun note(message: String) {
        journal.add("${clock.now()} $message")
    }

    fun advance(code: String) {
        val item = store.find(code) ?: error("global rest break budget $code not found")
        validator.requireValid(item)
        item.moveTo(GlobalRestBreakBudgetStatus.IN_REVIEW)
        store.save(item)
        note("$code is in review")
    }

    fun retire(code: String, reason: String) {
        val item = store.find(code) ?: error("global rest break budget $code not found")
        if (item.isTerminal()) {
            note("$code already closed")
            return
        }
        item.moveTo(GlobalRestBreakBudgetStatus.ARCHIVED)
        store.save(item)
        note("$code is archived: $reason")
    }

    fun evaluate(code: String): Double {
        val item = store.find(code) ?: error("global rest break budget $code not found")
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

fun newGlobalRestBreakBudgetDesk(store: GlobalRestBreakBudgetStore, clock: Clock, flags: Registry): GlobalRestBreakBudgetDesk {
    return GlobalRestBreakBudgetDesk(
        store = store,
        validator = defaultGlobalRestBreakBudgetValidator(),
        clock = clock,
        journal = mutableListOf<String>(),
        engine = defaultGlobalRestBreakBudgetEngine(flags),
    )
}
