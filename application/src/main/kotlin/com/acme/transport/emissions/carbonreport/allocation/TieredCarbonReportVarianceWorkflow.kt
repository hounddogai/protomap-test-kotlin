package com.acme.transport.emissions.carbonreport.allocation

import com.acme.transport.emissions.clock.Clock

/**
 * Moves tiered carbon report variances through their life cycle and keeps a journal of what it did.
 */
data class TieredCarbonReportVarianceManager(
    var store: TieredCarbonReportVarianceStore,
    var validator: TieredCarbonReportVarianceValidator,
    var clock: Clock,
    var journal: MutableList<String>,
) {
    private fun note(message: String) {
        journal.add("${clock.now()} $message")
    }

    fun open(code: String) {
        val item = store.find(code) ?: error("tiered carbon report variance $code not found")
        validator.requireValid(item)
        item.moveTo(TieredCarbonReportVarianceStatus.SCHEDULED)
        store.save(item)
        note("$code is scheduled")
    }

    fun settle(code: String, reason: String) {
        val item = store.find(code) ?: error("tiered carbon report variance $code not found")
        if (item.isTerminal()) {
            note("$code already closed")
            return
        }
        item.moveTo(TieredCarbonReportVarianceStatus.CANCELLED)
        store.save(item)
        note("$code is cancelled: $reason")
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

fun newTieredCarbonReportVarianceManager(store: TieredCarbonReportVarianceStore, clock: Clock): TieredCarbonReportVarianceManager {
    return TieredCarbonReportVarianceManager(
        store = store,
        validator = defaultTieredCarbonReportVarianceValidator(),
        clock = clock,
        journal = mutableListOf<String>(),
    )
}
