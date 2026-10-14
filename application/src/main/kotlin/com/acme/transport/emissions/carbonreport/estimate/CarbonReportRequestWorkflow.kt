package com.acme.transport.emissions.carbonreport.estimate

import com.acme.transport.emissions.clock.Clock

/** Moves carbon report requests through their life cycle and keeps a journal of what it did. */
data class CarbonReportRequestWorkflow(
    var store: CarbonReportRequestStore,
    var validator: CarbonReportRequestValidator,
    var clock: Clock,
    var journal: MutableList<String>,
) {
    private fun note(message: String) {
        journal.add("${clock.now()} $message")
    }

    fun submit(code: String) {
        val item = store.find(code) ?: error("carbon report request $code not found")
        validator.requireValid(item)
        item.moveTo(CarbonReportRequestStatus.SCHEDULED)
        store.save(item)
        note("$code is scheduled")
    }

    fun settle(code: String, reason: String) {
        val item = store.find(code) ?: error("carbon report request $code not found")
        if (item.isTerminal()) {
            note("$code already closed")
            return
        }
        item.moveTo(CarbonReportRequestStatus.CANCELLED)
        store.save(item)
        note("$code is cancelled: $reason")
    }

    fun reviewStale(cutoff: Int): Int {
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

fun newCarbonReportRequestWorkflow(store: CarbonReportRequestStore, clock: Clock): CarbonReportRequestWorkflow {
    return CarbonReportRequestWorkflow(
        store = store,
        validator = defaultCarbonReportRequestValidator(),
        clock = clock,
        journal = mutableListOf<String>(),
    )
}
