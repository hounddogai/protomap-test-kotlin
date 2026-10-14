package com.acme.transport.safety.incident.defaultincidentplan

import com.acme.transport.safety.clock.Clock

/** Moves incident mappings through their life cycle and keeps a journal of what it did. */
data class IncidentMappingCoordinator(
    var store: IncidentMappingStore,
    var validator: IncidentMappingValidator,
    var clock: Clock,
    var journal: MutableList<String>,
    var engine: IncidentMappingEngine,
) {
    private fun note(message: String) {
        journal.add("${clock.now()} $message")
    }

    fun advance(code: String) {
        val item = store.find(code) ?: error("incident mapping $code not found")
        validator.requireValid(item)
        item.moveTo(IncidentMappingStatus.QUEUED)
        store.save(item)
        note("$code is queued")
    }

    fun close(code: String, reason: String) {
        val item = store.find(code) ?: error("incident mapping $code not found")
        if (item.isTerminal()) {
            note("$code already closed")
            return
        }
        item.moveTo(IncidentMappingStatus.FAILED)
        store.save(item)
        note("$code is failed: $reason")
    }

    fun score(code: String): Double {
        val item = store.find(code) ?: error("incident mapping $code not found")
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

fun newIncidentMappingCoordinator(store: IncidentMappingStore, clock: Clock): IncidentMappingCoordinator {
    return IncidentMappingCoordinator(
        store = store,
        validator = defaultIncidentMappingValidator(),
        clock = clock,
        journal = mutableListOf<String>(),
        engine = defaultIncidentMappingEngine(),
    )
}
