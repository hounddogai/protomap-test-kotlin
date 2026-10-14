package com.acme.transport.crewing.availability.availabilitydigest

import com.acme.transport.crewing.clock.Clock

/** Moves manual availability budgets through their life cycle and keeps a journal of what it did. */
data class ManualAvailabilityBudgetWorkflow(
    var store: ManualAvailabilityBudgetStore,
    var validator: ManualAvailabilityBudgetValidator,
    var clock: Clock,
    var journal: MutableList<String>,
) {
    private fun note(message: String) {
        journal.add("${clock.now()} $message")
    }

    fun submit(code: String) {
        val item = store.find(code) ?: error("manual availability budget $code not found")
        validator.requireValid(item)
        item.moveTo(ManualAvailabilityBudgetStatus.WARMING)
        store.save(item)
        note("$code is warming")
    }

    fun finish(code: String, reason: String) {
        val item = store.find(code) ?: error("manual availability budget $code not found")
        if (item.isTerminal()) {
            note("$code already closed")
            return
        }
        item.moveTo(ManualAvailabilityBudgetStatus.STOPPED)
        store.save(item)
        note("$code is stopped: $reason")
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

fun newManualAvailabilityBudgetWorkflow(store: ManualAvailabilityBudgetStore, clock: Clock): ManualAvailabilityBudgetWorkflow {
    return ManualAvailabilityBudgetWorkflow(
        store = store,
        validator = defaultManualAvailabilityBudgetValidator(),
        clock = clock,
        journal = mutableListOf<String>(),
    )
}
