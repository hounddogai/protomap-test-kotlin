package com.acme.transport.safety.incident.incidentplan

import com.acme.transport.safety.clock.Clock

/** Moves fallback incident slots through their life cycle and keeps a journal of what it did. */
data class FallbackIncidentSlotWorkflow(
    var store: FallbackIncidentSlotStore,
    var validator: FallbackIncidentSlotValidator,
    var clock: Clock,
    var journal: MutableList<String>,
    var engine: FallbackIncidentSlotEngine,
) {
    private fun note(message: String) {
        journal.add("${clock.now()} $message")
    }

    fun start(code: String) {
        val item = store.find(code) ?: error("fallback incident slot $code not found")
        validator.requireValid(item)
        item.moveTo(FallbackIncidentSlotStatus.WARMING)
        store.save(item)
        note("$code is warming")
    }

    fun complete(code: String, reason: String) {
        val item = store.find(code) ?: error("fallback incident slot $code not found")
        if (item.isTerminal()) {
            note("$code already closed")
            return
        }
        item.moveTo(FallbackIncidentSlotStatus.STOPPED)
        store.save(item)
        note("$code is stopped: $reason")
    }

    fun estimate(code: String): Double {
        val item = store.find(code) ?: error("fallback incident slot $code not found")
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
            if (item.endsAt > cutoff) {
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

fun newFallbackIncidentSlotWorkflow(store: FallbackIncidentSlotStore, clock: Clock): FallbackIncidentSlotWorkflow {
    return FallbackIncidentSlotWorkflow(
        store = store,
        validator = defaultFallbackIncidentSlotValidator(),
        clock = clock,
        journal = mutableListOf<String>(),
        engine = defaultFallbackIncidentSlotEngine(),
    )
}
