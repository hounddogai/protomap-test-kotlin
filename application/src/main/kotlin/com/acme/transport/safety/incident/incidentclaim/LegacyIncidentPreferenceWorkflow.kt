package com.acme.transport.safety.incident.incidentclaim

import com.acme.transport.kit.flags.Registry
import com.acme.transport.safety.clock.Clock

/** Moves legacy incident preferences through their life cycle and keeps a journal of what it did. */
data class LegacyIncidentPreferenceDesk(
    var store: LegacyIncidentPreferenceStore,
    var validator: LegacyIncidentPreferenceValidator,
    var clock: Clock,
    var journal: MutableList<String>,
    var engine: LegacyIncidentPreferenceEngine,
) {
    private fun note(message: String) {
        journal.add("${clock.now()} $message")
    }

    fun start(code: String) {
        val item = store.find(code) ?: error("legacy incident preference $code not found")
        validator.requireValid(item)
        item.moveTo(LegacyIncidentPreferenceStatus.PENDING)
        store.save(item)
        note("$code is pending")
    }

    fun settle(code: String, reason: String) {
        val item = store.find(code) ?: error("legacy incident preference $code not found")
        if (item.isTerminal()) {
            note("$code already closed")
            return
        }
        item.moveTo(LegacyIncidentPreferenceStatus.CLOSED)
        store.save(item)
        note("$code is closed: $reason")
    }

    fun estimate(code: String): Double {
        val item = store.find(code) ?: error("legacy incident preference $code not found")
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

fun newLegacyIncidentPreferenceDesk(store: LegacyIncidentPreferenceStore, clock: Clock, flags: Registry): LegacyIncidentPreferenceDesk {
    return LegacyIncidentPreferenceDesk(
        store = store,
        validator = defaultLegacyIncidentPreferenceValidator(),
        clock = clock,
        journal = mutableListOf<String>(),
        engine = defaultLegacyIncidentPreferenceEngine(flags),
    )
}
