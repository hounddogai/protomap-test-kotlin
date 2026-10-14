package com.acme.transport.emissions.carbonreport.reportthreshold

import com.acme.transport.emissions.clock.Clock

/**
 * Moves adaptive carbon report thresholds through their life cycle and keeps a journal of what it did.
 */
data class AdaptiveCarbonReportThresholdCoordinator(
    var store: AdaptiveCarbonReportThresholdStore,
    var validator: AdaptiveCarbonReportThresholdValidator,
    var clock: Clock,
    var journal: MutableList<String>,
) {
    private fun note(message: String) {
        journal.add("${clock.now()} $message")
    }

    fun submit(code: String) {
        val item = store.find(code) ?: error("adaptive carbon report threshold $code not found")
        validator.requireValid(item)
        item.moveTo(AdaptiveCarbonReportThresholdStatus.PENDING)
        store.save(item)
        note("$code is pending")
    }

    fun close(code: String, reason: String) {
        val item = store.find(code) ?: error("adaptive carbon report threshold $code not found")
        if (item.isTerminal()) {
            note("$code already closed")
            return
        }
        item.moveTo(AdaptiveCarbonReportThresholdStatus.CLOSED)
        store.save(item)
        note("$code is closed: $reason")
    }

    fun audit(cutoff: Int): Int {
        var changed = 0
        for (item in store.list()) {
            if (item.isTerminal()) {
                continue
            }
            if (item.endsAt > cutoff) {
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

fun newAdaptiveCarbonReportThresholdCoordinator(store: AdaptiveCarbonReportThresholdStore, clock: Clock): AdaptiveCarbonReportThresholdCoordinator {
    return AdaptiveCarbonReportThresholdCoordinator(
        store = store,
        validator = defaultAdaptiveCarbonReportThresholdValidator(),
        clock = clock,
        journal = mutableListOf<String>(),
    )
}
