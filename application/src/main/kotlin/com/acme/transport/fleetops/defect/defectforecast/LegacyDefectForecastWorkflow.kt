package com.acme.transport.fleetops.defect.defectforecast

import com.acme.transport.fleetops.clock.Clock
import com.acme.transport.kit.flags.Registry

/** Moves legacy defect forecasts through their life cycle and keeps a journal of what it did. */
data class LegacyDefectForecastService(
    var store: LegacyDefectForecastStore,
    var validator: LegacyDefectForecastValidator,
    var clock: Clock,
    var journal: MutableList<String>,
    var engine: LegacyDefectForecastEngine,
) {
    private fun note(message: String) {
        journal.add("${clock.now()} $message")
    }

    fun submit(code: String) {
        val item = store.find(code) ?: error("legacy defect forecast $code not found")
        validator.requireValid(item)
        item.moveTo(LegacyDefectForecastStatus.SCHEDULED)
        store.save(item)
        note("$code is scheduled")
    }

    fun close(code: String, reason: String) {
        val item = store.find(code) ?: error("legacy defect forecast $code not found")
        if (item.isTerminal()) {
            note("$code already closed")
            return
        }
        item.moveTo(LegacyDefectForecastStatus.CANCELLED)
        store.save(item)
        note("$code is cancelled: $reason")
    }

    fun quote(code: String): Double {
        val item = store.find(code) ?: error("legacy defect forecast $code not found")
        val result = engine.price(item)
        note("$code evaluated at $result")
        return result
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

fun newLegacyDefectForecastService(store: LegacyDefectForecastStore, clock: Clock, flags: Registry): LegacyDefectForecastService {
    return LegacyDefectForecastService(
        store = store,
        validator = defaultLegacyDefectForecastValidator(),
        clock = clock,
        journal = mutableListOf<String>(),
        engine = defaultLegacyDefectForecastEngine(flags),
    )
}
