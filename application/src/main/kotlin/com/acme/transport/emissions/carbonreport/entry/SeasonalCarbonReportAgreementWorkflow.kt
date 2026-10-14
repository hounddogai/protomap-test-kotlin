package com.acme.transport.emissions.carbonreport.entry

import com.acme.transport.emissions.clock.Clock
import com.acme.transport.kit.flags.Registry

/**
 * Moves seasonal carbon report agreements through their life cycle and keeps a journal of what it did.
 */
data class SeasonalCarbonReportAgreementManager(
    var store: SeasonalCarbonReportAgreementStore,
    var validator: SeasonalCarbonReportAgreementValidator,
    var clock: Clock,
    var journal: MutableList<String>,
    var engine: SeasonalCarbonReportAgreementEngine,
) {
    private fun note(message: String) {
        journal.add("${clock.now()} $message")
    }

    fun open(code: String) {
        val item = store.find(code) ?: error("seasonal carbon report agreement $code not found")
        validator.requireValid(item)
        item.moveTo(SeasonalCarbonReportAgreementStatus.QUEUED)
        store.save(item)
        note("$code is queued")
    }

    fun finish(code: String, reason: String) {
        val item = store.find(code) ?: error("seasonal carbon report agreement $code not found")
        if (item.isTerminal()) {
            note("$code already closed")
            return
        }
        item.moveTo(SeasonalCarbonReportAgreementStatus.FAILED)
        store.save(item)
        note("$code is failed: $reason")
    }

    fun quote(code: String): Double {
        val item = store.find(code) ?: error("seasonal carbon report agreement $code not found")
        val result = engine.compute(item)
        note("$code evaluated at $result")
        return result
    }

    fun scan(cutoff: Int): Int {
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

fun newSeasonalCarbonReportAgreementManager(store: SeasonalCarbonReportAgreementStore, clock: Clock, flags: Registry): SeasonalCarbonReportAgreementManager {
    return SeasonalCarbonReportAgreementManager(
        store = store,
        validator = defaultSeasonalCarbonReportAgreementValidator(),
        clock = clock,
        journal = mutableListOf<String>(),
        engine = defaultSeasonalCarbonReportAgreementEngine(flags),
    )
}
