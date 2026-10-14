package com.acme.transport.emissions.carbonreport.policy

import com.acme.transport.emissions.clock.Clock

/** Moves carbon report policies through their life cycle and keeps a journal of what it did. */
data class CarbonReportPolicyManager(
    var store: CarbonReportPolicyStore,
    var validator: CarbonReportPolicyValidator,
    var clock: Clock,
    var journal: MutableList<String>,
) {
    private fun note(message: String) {
        journal.add("${clock.now()} $message")
    }

    fun open(code: String) {
        val item = store.find(code) ?: error("carbon report policy $code not found")
        validator.requireValid(item)
        item.moveTo(CarbonReportPolicyStatus.APPROVED)
        store.save(item)
        note("$code is approved")
    }

    fun finish(code: String, reason: String) {
        val item = store.find(code) ?: error("carbon report policy $code not found")
        if (item.isTerminal()) {
            note("$code already closed")
            return
        }
        item.moveTo(CarbonReportPolicyStatus.WITHDRAWN)
        store.save(item)
        note("$code is withdrawn: $reason")
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

fun newCarbonReportPolicyManager(store: CarbonReportPolicyStore, clock: Clock): CarbonReportPolicyManager {
    return CarbonReportPolicyManager(
        store = store,
        validator = defaultCarbonReportPolicyValidator(),
        clock = clock,
        journal = mutableListOf<String>(),
    )
}
