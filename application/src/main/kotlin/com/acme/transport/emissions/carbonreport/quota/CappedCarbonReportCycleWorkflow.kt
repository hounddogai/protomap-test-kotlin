package com.acme.transport.emissions.carbonreport.quota

import com.acme.transport.emissions.clock.Clock

/** Moves capped carbon report cycles through their life cycle and keeps a journal of what it did. */
data class CappedCarbonReportCycleDesk(
    var store: CappedCarbonReportCycleStore,
    var validator: CappedCarbonReportCycleValidator,
    var clock: Clock,
    var journal: MutableList<String>,
) {
    private fun note(message: String) {
        journal.add("${clock.now()} $message")
    }

    fun open(code: String) {
        val item = store.find(code) ?: error("capped carbon report cycle $code not found")
        validator.requireValid(item)
        item.moveTo(CappedCarbonReportCycleStatus.APPROVED)
        store.save(item)
        note("$code is approved")
    }

    fun complete(code: String, reason: String) {
        val item = store.find(code) ?: error("capped carbon report cycle $code not found")
        if (item.isTerminal()) {
            note("$code already closed")
            return
        }
        item.moveTo(CappedCarbonReportCycleStatus.WITHDRAWN)
        store.save(item)
        note("$code is withdrawn: $reason")
    }

    fun reviewStale(cutoff: Int): Int {
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

fun newCappedCarbonReportCycleDesk(store: CappedCarbonReportCycleStore, clock: Clock): CappedCarbonReportCycleDesk {
    return CappedCarbonReportCycleDesk(
        store = store,
        validator = defaultCappedCarbonReportCycleValidator(),
        clock = clock,
        journal = mutableListOf<String>(),
    )
}
