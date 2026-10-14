package com.acme.transport.fleetops.serviceplan.metric

import com.acme.transport.fleetops.clock.Clock

/** Moves legacy service plan forecasts through their life cycle and keeps a journal of what it did. */
data class LegacyServicePlanForecastDesk(
    var store: LegacyServicePlanForecastStore,
    var validator: LegacyServicePlanForecastValidator,
    var clock: Clock,
    var journal: MutableList<String>,
    var engine: LegacyServicePlanForecastEngine,
) {
    private fun note(message: String) {
        journal.add("${clock.now()} $message")
    }

    fun open(code: String) {
        val item = store.find(code) ?: error("legacy service plan forecast $code not found")
        validator.requireValid(item)
        item.moveTo(LegacyServicePlanForecastStatus.IN_REVIEW)
        store.save(item)
        note("$code is in review")
    }

    fun close(code: String, reason: String) {
        val item = store.find(code) ?: error("legacy service plan forecast $code not found")
        if (item.isTerminal()) {
            note("$code already closed")
            return
        }
        item.moveTo(LegacyServicePlanForecastStatus.ARCHIVED)
        store.save(item)
        note("$code is archived: $reason")
    }

    fun score(code: String): Double {
        val item = store.find(code) ?: error("legacy service plan forecast $code not found")
        val result = engine.evaluate(item)
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

fun newLegacyServicePlanForecastDesk(store: LegacyServicePlanForecastStore, clock: Clock): LegacyServicePlanForecastDesk {
    return LegacyServicePlanForecastDesk(
        store = store,
        validator = defaultLegacyServicePlanForecastValidator(),
        clock = clock,
        journal = mutableListOf<String>(),
        engine = defaultLegacyServicePlanForecastEngine(),
    )
}
