package com.acme.transport.emissions.carbonreport.reportbudget

import com.acme.transport.emissions.clock.Clock

/**
 * Moves fallback carbon report budgets through their life cycle and keeps a journal of what it did.
 */
data class FallbackCarbonReportBudgetWorkflow(
    var store: FallbackCarbonReportBudgetStore,
    var validator: FallbackCarbonReportBudgetValidator,
    var clock: Clock,
    var journal: MutableList<String>,
) {
    private fun note(message: String) {
        journal.add("${clock.now()} $message")
    }

    fun submit(code: String) {
        val item = store.find(code) ?: error("fallback carbon report budget $code not found")
        validator.requireValid(item)
        item.moveTo(FallbackCarbonReportBudgetStatus.GRANTED)
        store.save(item)
        note("$code is granted")
    }

    fun settle(code: String, reason: String) {
        val item = store.find(code) ?: error("fallback carbon report budget $code not found")
        if (item.isTerminal()) {
            note("$code already closed")
            return
        }
        item.moveTo(FallbackCarbonReportBudgetStatus.EXPIRED)
        store.save(item)
        note("$code is expired: $reason")
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

fun newFallbackCarbonReportBudgetWorkflow(store: FallbackCarbonReportBudgetStore, clock: Clock): FallbackCarbonReportBudgetWorkflow {
    return FallbackCarbonReportBudgetWorkflow(
        store = store,
        validator = defaultFallbackCarbonReportBudgetValidator(),
        clock = clock,
        journal = mutableListOf<String>(),
    )
}
