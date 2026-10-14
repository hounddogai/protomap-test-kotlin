package com.acme.transport.safety.incident.preference

import com.acme.transport.kit.flags.Registry
import com.acme.transport.safety.clock.Clock

/** Moves scheduled incident forecasts through their life cycle and keeps a journal of what it did. */
data class ScheduledIncidentForecastManager(
    var store: ScheduledIncidentForecastStore,
    var validator: ScheduledIncidentForecastValidator,
    var clock: Clock,
    var journal: MutableList<String>,
    var engine: ScheduledIncidentForecastEngine,
) {
    private fun note(message: String) {
        journal.add("${clock.now()} $message")
    }

    fun submit(code: String) {
        val item = store.find(code) ?: error("scheduled incident forecast $code not found")
        validator.requireValid(item)
        item.moveTo(ScheduledIncidentForecastStatus.VALIDATED)
        store.save(item)
        note("$code is validated")
    }

    fun close(code: String, reason: String) {
        val item = store.find(code) ?: error("scheduled incident forecast $code not found")
        if (item.isTerminal()) {
            note("$code already closed")
            return
        }
        item.moveTo(ScheduledIncidentForecastStatus.RETIRED)
        store.save(item)
        note("$code is retired: $reason")
    }

    fun estimate(code: String): Double {
        val item = store.find(code) ?: error("scheduled incident forecast $code not found")
        val result = engine.evaluate(item)
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

fun newScheduledIncidentForecastManager(store: ScheduledIncidentForecastStore, clock: Clock, flags: Registry): ScheduledIncidentForecastManager {
    return ScheduledIncidentForecastManager(
        store = store,
        validator = defaultScheduledIncidentForecastValidator(),
        clock = clock,
        journal = mutableListOf<String>(),
        engine = defaultScheduledIncidentForecastEngine(flags),
    )
}
