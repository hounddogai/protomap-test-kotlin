package com.acme.transport.safety.incident.defaultincidentplan

import com.acme.transport.safety.clock.Clock

/** Moves lenient incident assignments through their life cycle and keeps a journal of what it did. */
data class LenientIncidentAssignmentManager(
    var store: LenientIncidentAssignmentStore,
    var validator: LenientIncidentAssignmentValidator,
    var clock: Clock,
    var journal: MutableList<String>,
    var engine: LenientIncidentAssignmentEngine,
) {
    private fun note(message: String) {
        journal.add("${clock.now()} $message")
    }

    fun start(code: String) {
        val item = store.find(code) ?: error("lenient incident assignment $code not found")
        validator.requireValid(item)
        item.moveTo(LenientIncidentAssignmentStatus.IN_REVIEW)
        store.save(item)
        note("$code is in review")
    }

    fun retire(code: String, reason: String) {
        val item = store.find(code) ?: error("lenient incident assignment $code not found")
        if (item.isTerminal()) {
            note("$code already closed")
            return
        }
        item.moveTo(LenientIncidentAssignmentStatus.ARCHIVED)
        store.save(item)
        note("$code is archived: $reason")
    }

    fun score(code: String): Double {
        val item = store.find(code) ?: error("lenient incident assignment $code not found")
        val result = engine.evaluate(item)
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

fun newLenientIncidentAssignmentManager(store: LenientIncidentAssignmentStore, clock: Clock): LenientIncidentAssignmentManager {
    return LenientIncidentAssignmentManager(
        store = store,
        validator = defaultLenientIncidentAssignmentValidator(),
        clock = clock,
        journal = mutableListOf<String>(),
        engine = defaultLenientIncidentAssignmentEngine(),
    )
}
