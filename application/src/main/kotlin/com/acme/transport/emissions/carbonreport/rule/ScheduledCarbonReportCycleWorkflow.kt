package com.acme.transport.emissions.carbonreport.rule

import com.acme.transport.emissions.clock.Clock

/**
 * Moves scheduled carbon report cycles through their life cycle and keeps a journal of what it did.
 */
data class ScheduledCarbonReportCycleService(
    var store: ScheduledCarbonReportCycleStore,
    var validator: ScheduledCarbonReportCycleValidator,
    var clock: Clock,
    var journal: MutableList<String>,
) {
    private fun note(message: String) {
        journal.add("${clock.now()} $message")
    }

    fun submit(code: String) {
        val item = store.find(code) ?: error("scheduled carbon report cycle $code not found")
        validator.requireValid(item)
        item.moveTo(ScheduledCarbonReportCycleStatus.PENDING)
        store.save(item)
        note("$code is pending")
    }

    fun finish(code: String, reason: String) {
        val item = store.find(code) ?: error("scheduled carbon report cycle $code not found")
        if (item.isTerminal()) {
            note("$code already closed")
            return
        }
        item.moveTo(ScheduledCarbonReportCycleStatus.CLOSED)
        store.save(item)
        note("$code is closed: $reason")
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

fun newScheduledCarbonReportCycleService(store: ScheduledCarbonReportCycleStore, clock: Clock): ScheduledCarbonReportCycleService {
    return ScheduledCarbonReportCycleService(
        store = store,
        validator = defaultScheduledCarbonReportCycleValidator(),
        clock = clock,
        journal = mutableListOf<String>(),
    )
}
