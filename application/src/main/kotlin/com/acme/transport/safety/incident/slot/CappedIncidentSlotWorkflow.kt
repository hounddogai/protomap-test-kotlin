package com.acme.transport.safety.incident.slot

import com.acme.transport.kit.flags.Registry
import com.acme.transport.safety.clock.Clock

/** Moves capped incident slots through their life cycle and keeps a journal of what it did. */
data class CappedIncidentSlotManager(
    var store: CappedIncidentSlotStore,
    var validator: CappedIncidentSlotValidator,
    var clock: Clock,
    var journal: MutableList<String>,
    var engine: CappedIncidentSlotEngine,
) {
    private fun note(message: String) {
        journal.add("${clock.now()} $message")
    }

    fun submit(code: String) {
        val item = store.find(code) ?: error("capped incident slot $code not found")
        validator.requireValid(item)
        item.moveTo(CappedIncidentSlotStatus.PENDING)
        store.save(item)
        note("$code is pending")
    }

    fun settle(code: String, reason: String) {
        val item = store.find(code) ?: error("capped incident slot $code not found")
        if (item.isTerminal()) {
            note("$code already closed")
            return
        }
        item.moveTo(CappedIncidentSlotStatus.CLOSED)
        store.save(item)
        note("$code is closed: $reason")
    }

    fun quote(code: String): Double {
        val item = store.find(code) ?: error("capped incident slot $code not found")
        val result = engine.compute(item)
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

fun newCappedIncidentSlotManager(store: CappedIncidentSlotStore, clock: Clock, flags: Registry): CappedIncidentSlotManager {
    return CappedIncidentSlotManager(
        store = store,
        validator = defaultCappedIncidentSlotValidator(),
        clock = clock,
        journal = mutableListOf<String>(),
        engine = defaultCappedIncidentSlotEngine(flags),
    )
}
