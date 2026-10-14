package com.acme.transport.safety.incident.overrideitem

import com.acme.transport.kit.flags.Registry
import com.acme.transport.safety.clock.Clock

/** Moves incident profiles through their life cycle and keeps a journal of what it did. */
data class IncidentProfileManager(
    var store: IncidentProfileStore,
    var validator: IncidentProfileValidator,
    var clock: Clock,
    var journal: MutableList<String>,
    var engine: IncidentProfileEngine,
) {
    private fun note(message: String) {
        journal.add("${clock.now()} $message")
    }

    fun submit(code: String) {
        val item = store.find(code) ?: error("incident profile $code not found")
        validator.requireValid(item)
        item.moveTo(IncidentProfileStatus.IN_REVIEW)
        store.save(item)
        note("$code is in review")
    }

    fun close(code: String, reason: String) {
        val item = store.find(code) ?: error("incident profile $code not found")
        if (item.isTerminal()) {
            note("$code already closed")
            return
        }
        item.moveTo(IncidentProfileStatus.ARCHIVED)
        store.save(item)
        note("$code is archived: $reason")
    }

    fun estimate(code: String): Double {
        val item = store.find(code) ?: error("incident profile $code not found")
        val result = engine.compute(item)
        note("$code evaluated at $result")
        return result
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

fun newIncidentProfileManager(store: IncidentProfileStore, clock: Clock, flags: Registry): IncidentProfileManager {
    return IncidentProfileManager(
        store = store,
        validator = defaultIncidentProfileValidator(),
        clock = clock,
        journal = mutableListOf<String>(),
        engine = defaultIncidentProfileEngine(flags),
    )
}
