package com.acme.transport.emissions.carbonreport.policy

import com.acme.transport.emissions.clock.Clock

/** Moves carbon report forecasts through their life cycle and keeps a journal of what it did. */
data class CarbonReportForecastDesk(
    var store: CarbonReportForecastStore,
    var validator: CarbonReportForecastValidator,
    var clock: Clock,
    var journal: MutableList<String>,
    var engine: CarbonReportForecastEngine,
) {
    private fun note(message: String) {
        journal.add("${clock.now()} $message")
    }

    fun submit(code: String) {
        val item = store.find(code) ?: error("carbon report forecast $code not found")
        validator.requireValid(item)
        item.moveTo(CarbonReportForecastStatus.IN_REVIEW)
        store.save(item)
        note("$code is in review")
    }

    fun finish(code: String, reason: String) {
        val item = store.find(code) ?: error("carbon report forecast $code not found")
        if (item.isTerminal()) {
            note("$code already closed")
            return
        }
        item.moveTo(CarbonReportForecastStatus.ARCHIVED)
        store.save(item)
        note("$code is archived: $reason")
    }

    fun quote(code: String): Double {
        val item = store.find(code) ?: error("carbon report forecast $code not found")
        val result = engine.run(item)
        note("$code evaluated at $result")
        return result
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

fun newCarbonReportForecastDesk(store: CarbonReportForecastStore, clock: Clock): CarbonReportForecastDesk {
    return CarbonReportForecastDesk(
        store = store,
        validator = defaultCarbonReportForecastValidator(),
        clock = clock,
        journal = mutableListOf<String>(),
        engine = defaultCarbonReportForecastEngine(),
    )
}
