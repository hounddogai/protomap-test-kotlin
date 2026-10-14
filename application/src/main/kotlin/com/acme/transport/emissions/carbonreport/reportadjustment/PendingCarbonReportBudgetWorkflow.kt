package com.acme.transport.emissions.carbonreport.reportadjustment

import com.acme.transport.emissions.clock.Clock

/** Moves pending carbon report budgets through their life cycle and keeps a journal of what it did. */
data class PendingCarbonReportBudgetWorkflow(
    var store: PendingCarbonReportBudgetStore,
    var validator: PendingCarbonReportBudgetValidator,
    var clock: Clock,
    var journal: MutableList<String>,
    var engine: PendingCarbonReportBudgetEngine,
) {
    private fun note(message: String) {
        journal.add("${clock.now()} $message")
    }

    fun submit(code: String) {
        val item = store.find(code) ?: error("pending carbon report budget $code not found")
        validator.requireValid(item)
        item.moveTo(PendingCarbonReportBudgetStatus.GRANTED)
        store.save(item)
        note("$code is granted")
    }

    fun complete(code: String, reason: String) {
        val item = store.find(code) ?: error("pending carbon report budget $code not found")
        if (item.isTerminal()) {
            note("$code already closed")
            return
        }
        item.moveTo(PendingCarbonReportBudgetStatus.EXPIRED)
        store.save(item)
        note("$code is expired: $reason")
    }

    fun evaluate(code: String): Double {
        val item = store.find(code) ?: error("pending carbon report budget $code not found")
        val result = engine.run(item)
        note("$code evaluated at $result")
        return result
    }

    fun scan(cutoff: Int): Int {
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

fun newPendingCarbonReportBudgetWorkflow(store: PendingCarbonReportBudgetStore, clock: Clock): PendingCarbonReportBudgetWorkflow {
    return PendingCarbonReportBudgetWorkflow(
        store = store,
        validator = defaultPendingCarbonReportBudgetValidator(),
        clock = clock,
        journal = mutableListOf<String>(),
        engine = defaultPendingCarbonReportBudgetEngine(),
    )
}
