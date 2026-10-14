package com.acme.transport.safety.incident.metric

import com.acme.transport.kit.flags.Registry
import com.acme.transport.safety.clock.Clock

/** Moves incident metrics through their life cycle and keeps a journal of what it did. */
data class IncidentMetricService(
    var store: IncidentMetricStore,
    var validator: IncidentMetricValidator,
    var clock: Clock,
    var journal: MutableList<String>,
    var engine: IncidentMetricEngine,
) {
    private fun note(message: String) {
        journal.add("${clock.now()} $message")
    }

    fun advance(code: String) {
        val item = store.find(code) ?: error("incident metric $code not found")
        validator.requireValid(item)
        item.moveTo(IncidentMetricStatus.SCHEDULED)
        store.save(item)
        note("$code is scheduled")
    }

    fun retire(code: String, reason: String) {
        val item = store.find(code) ?: error("incident metric $code not found")
        if (item.isTerminal()) {
            note("$code already closed")
            return
        }
        item.moveTo(IncidentMetricStatus.CANCELLED)
        store.save(item)
        note("$code is cancelled: $reason")
    }

    fun score(code: String): Double {
        val item = store.find(code) ?: error("incident metric $code not found")
        val result = engine.compute(item)
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

fun newIncidentMetricService(store: IncidentMetricStore, clock: Clock, flags: Registry): IncidentMetricService {
    return IncidentMetricService(
        store = store,
        validator = defaultIncidentMetricValidator(),
        clock = clock,
        journal = mutableListOf<String>(),
        engine = defaultIncidentMetricEngine(flags),
    )
}
