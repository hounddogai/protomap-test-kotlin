package com.acme.transport.emissions.carbonreport.reportbudget

import com.acme.transport.emissions.clock.Clock

/** Moves manual carbon report cycles through their life cycle and keeps a journal of what it did. */
data class ManualCarbonReportCycleCoordinator(
    var store: ManualCarbonReportCycleStore,
    var validator: ManualCarbonReportCycleValidator,
    var clock: Clock,
    var journal: MutableList<String>,
) {
    private fun note(message: String) {
        journal.add("${clock.now()} $message")
    }

    fun advance(code: String) {
        val item = store.find(code) ?: error("manual carbon report cycle $code not found")
        validator.requireValid(item)
        item.moveTo(ManualCarbonReportCycleStatus.SCHEDULED)
        store.save(item)
        note("$code is scheduled")
    }

    fun close(code: String, reason: String) {
        val item = store.find(code) ?: error("manual carbon report cycle $code not found")
        if (item.isTerminal()) {
            note("$code already closed")
            return
        }
        item.moveTo(ManualCarbonReportCycleStatus.CANCELLED)
        store.save(item)
        note("$code is cancelled: $reason")
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

fun newManualCarbonReportCycleCoordinator(store: ManualCarbonReportCycleStore, clock: Clock): ManualCarbonReportCycleCoordinator {
    return ManualCarbonReportCycleCoordinator(
        store = store,
        validator = defaultManualCarbonReportCycleValidator(),
        clock = clock,
        journal = mutableListOf<String>(),
    )
}
