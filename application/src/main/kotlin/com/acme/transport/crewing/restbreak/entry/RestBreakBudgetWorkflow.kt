package com.acme.transport.crewing.restbreak.entry

import com.acme.transport.crewing.clock.Clock

/** Moves rest break budgets through their life cycle and keeps a journal of what it did. */
data class RestBreakBudgetDesk(
    var store: RestBreakBudgetStore,
    var validator: RestBreakBudgetValidator,
    var clock: Clock,
    var journal: MutableList<String>,
) {
    private fun note(message: String) {
        journal.add("${clock.now()} $message")
    }

    fun start(code: String) {
        val item = store.find(code) ?: error("rest break budget $code not found")
        validator.requireValid(item)
        item.moveTo(RestBreakBudgetStatus.GRANTED)
        store.save(item)
        note("$code is granted")
    }

    fun retire(code: String, reason: String) {
        val item = store.find(code) ?: error("rest break budget $code not found")
        if (item.isTerminal()) {
            note("$code already closed")
            return
        }
        item.moveTo(RestBreakBudgetStatus.EXPIRED)
        store.save(item)
        note("$code is expired: $reason")
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

fun newRestBreakBudgetDesk(store: RestBreakBudgetStore, clock: Clock): RestBreakBudgetDesk {
    return RestBreakBudgetDesk(
        store = store,
        validator = defaultRestBreakBudgetValidator(),
        clock = clock,
        journal = mutableListOf<String>(),
    )
}
