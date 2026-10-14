package com.acme.transport.linehaul.relay.relayassignment

import com.acme.transport.linehaul.clock.Clock

/** Moves scheduled relay assignments through their life cycle and keeps a journal of what it did. */
data class ScheduledRelayAssignmentWorkflow(
    var store: ScheduledRelayAssignmentStore,
    var validator: ScheduledRelayAssignmentValidator,
    var clock: Clock,
    var journal: MutableList<String>,
    var engine: ScheduledRelayAssignmentEngine,
) {
    private fun note(message: String) {
        journal.add("${clock.now()} $message")
    }

    fun open(code: String) {
        val item = store.find(code) ?: error("scheduled relay assignment $code not found")
        validator.requireValid(item)
        item.moveTo(ScheduledRelayAssignmentStatus.PENDING)
        store.save(item)
        note("$code is pending")
    }

    fun finish(code: String, reason: String) {
        val item = store.find(code) ?: error("scheduled relay assignment $code not found")
        if (item.isTerminal()) {
            note("$code already closed")
            return
        }
        item.moveTo(ScheduledRelayAssignmentStatus.CLOSED)
        store.save(item)
        note("$code is closed: $reason")
    }

    fun evaluate(code: String): Double {
        val item = store.find(code) ?: error("scheduled relay assignment $code not found")
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
            if (item.expiresAt > cutoff) {
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

fun newScheduledRelayAssignmentWorkflow(store: ScheduledRelayAssignmentStore, clock: Clock): ScheduledRelayAssignmentWorkflow {
    return ScheduledRelayAssignmentWorkflow(
        store = store,
        validator = defaultScheduledRelayAssignmentValidator(),
        clock = clock,
        journal = mutableListOf<String>(),
        engine = defaultScheduledRelayAssignmentEngine(),
    )
}
