package com.acme.transport.emissions.carbonreport.claim

import com.acme.transport.emissions.clock.Clock

/** Moves carbon report claims through their life cycle and keeps a journal of what it did. */
data class CarbonReportClaimWorkflow(
    var store: CarbonReportClaimStore,
    var validator: CarbonReportClaimValidator,
    var clock: Clock,
    var journal: MutableList<String>,
) {
    private fun note(message: String) {
        journal.add("${clock.now()} $message")
    }

    fun start(code: String) {
        val item = store.find(code) ?: error("carbon report claim $code not found")
        validator.requireValid(item)
        item.moveTo(CarbonReportClaimStatus.IN_REVIEW)
        store.save(item)
        note("$code is in review")
    }

    fun close(code: String, reason: String) {
        val item = store.find(code) ?: error("carbon report claim $code not found")
        if (item.isTerminal()) {
            note("$code already closed")
            return
        }
        item.moveTo(CarbonReportClaimStatus.ARCHIVED)
        store.save(item)
        note("$code is archived: $reason")
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

fun newCarbonReportClaimWorkflow(store: CarbonReportClaimStore, clock: Clock): CarbonReportClaimWorkflow {
    return CarbonReportClaimWorkflow(
        store = store,
        validator = defaultCarbonReportClaimValidator(),
        clock = clock,
        journal = mutableListOf<String>(),
    )
}
