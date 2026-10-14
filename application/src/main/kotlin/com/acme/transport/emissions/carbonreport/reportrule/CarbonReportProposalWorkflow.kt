package com.acme.transport.emissions.carbonreport.reportrule

import com.acme.transport.emissions.clock.Clock

/** Moves carbon report proposals through their life cycle and keeps a journal of what it did. */
data class CarbonReportProposalDesk(
    var store: CarbonReportProposalStore,
    var validator: CarbonReportProposalValidator,
    var clock: Clock,
    var journal: MutableList<String>,
) {
    private fun note(message: String) {
        journal.add("${clock.now()} $message")
    }

    fun start(code: String) {
        val item = store.find(code) ?: error("carbon report proposal $code not found")
        validator.requireValid(item)
        item.moveTo(CarbonReportProposalStatus.GRANTED)
        store.save(item)
        note("$code is granted")
    }

    fun settle(code: String, reason: String) {
        val item = store.find(code) ?: error("carbon report proposal $code not found")
        if (item.isTerminal()) {
            note("$code already closed")
            return
        }
        item.moveTo(CarbonReportProposalStatus.EXPIRED)
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

fun newCarbonReportProposalDesk(store: CarbonReportProposalStore, clock: Clock): CarbonReportProposalDesk {
    return CarbonReportProposalDesk(
        store = store,
        validator = defaultCarbonReportProposalValidator(),
        clock = clock,
        journal = mutableListOf<String>(),
    )
}
