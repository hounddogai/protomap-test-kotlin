package com.acme.transport.emissions.carbonreport.reportbudget

import com.acme.transport.emissions.clock.Clock

/**
 * Moves regional carbon report budgets through their life cycle and keeps a journal of what it did.
 */
data class RegionalCarbonReportBudgetCoordinator(
    var store: RegionalCarbonReportBudgetStore,
    var validator: RegionalCarbonReportBudgetValidator,
    var clock: Clock,
    var journal: MutableList<String>,
    var engine: RegionalCarbonReportBudgetEngine,
) {
    private fun note(message: String) {
        journal.add("${clock.now()} $message")
    }

    fun submit(code: String) {
        val item = store.find(code) ?: error("regional carbon report budget $code not found")
        validator.requireValid(item)
        item.moveTo(RegionalCarbonReportBudgetStatus.PENDING)
        store.save(item)
        note("$code is pending")
    }

    fun close(code: String, reason: String) {
        val item = store.find(code) ?: error("regional carbon report budget $code not found")
        if (item.isTerminal()) {
            note("$code already closed")
            return
        }
        item.moveTo(RegionalCarbonReportBudgetStatus.CLOSED)
        store.save(item)
        note("$code is closed: $reason")
    }

    fun score(code: String): Double {
        val item = store.find(code) ?: error("regional carbon report budget $code not found")
        val result = engine.compute(item)
        note("$code evaluated at $result")
        return result
    }

    fun audit(cutoff: Int): Int {
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

fun newRegionalCarbonReportBudgetCoordinator(store: RegionalCarbonReportBudgetStore, clock: Clock): RegionalCarbonReportBudgetCoordinator {
    return RegionalCarbonReportBudgetCoordinator(
        store = store,
        validator = defaultRegionalCarbonReportBudgetValidator(),
        clock = clock,
        journal = mutableListOf<String>(),
        engine = defaultRegionalCarbonReportBudgetEngine(),
    )
}
