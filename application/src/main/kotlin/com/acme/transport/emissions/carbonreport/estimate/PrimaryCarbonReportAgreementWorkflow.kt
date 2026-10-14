package com.acme.transport.emissions.carbonreport.estimate

import com.acme.transport.emissions.clock.Clock

/**
 * Moves primary carbon report agreements through their life cycle and keeps a journal of what it did.
 */
data class PrimaryCarbonReportAgreementCoordinator(
    var store: PrimaryCarbonReportAgreementStore,
    var validator: PrimaryCarbonReportAgreementValidator,
    var clock: Clock,
    var journal: MutableList<String>,
) {
    private fun note(message: String) {
        journal.add("${clock.now()} $message")
    }

    fun open(code: String) {
        val item = store.find(code) ?: error("primary carbon report agreement $code not found")
        validator.requireValid(item)
        item.moveTo(PrimaryCarbonReportAgreementStatus.IN_REVIEW)
        store.save(item)
        note("$code is in review")
    }

    fun close(code: String, reason: String) {
        val item = store.find(code) ?: error("primary carbon report agreement $code not found")
        if (item.isTerminal()) {
            note("$code already closed")
            return
        }
        item.moveTo(PrimaryCarbonReportAgreementStatus.ARCHIVED)
        store.save(item)
        note("$code is archived: $reason")
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

fun newPrimaryCarbonReportAgreementCoordinator(store: PrimaryCarbonReportAgreementStore, clock: Clock): PrimaryCarbonReportAgreementCoordinator {
    return PrimaryCarbonReportAgreementCoordinator(
        store = store,
        validator = defaultPrimaryCarbonReportAgreementValidator(),
        clock = clock,
        journal = mutableListOf<String>(),
    )
}
