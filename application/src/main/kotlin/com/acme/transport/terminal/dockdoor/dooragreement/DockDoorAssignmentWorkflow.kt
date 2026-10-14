package com.acme.transport.terminal.dockdoor.dooragreement

import com.acme.transport.terminal.clock.Clock

/** Moves dock door assignments through their life cycle and keeps a journal of what it did. */
data class DockDoorAssignmentService(
    var store: DockDoorAssignmentStore,
    var validator: DockDoorAssignmentValidator,
    var clock: Clock,
    var journal: MutableList<String>,
    var engine: DockDoorAssignmentEngine,
) {
    private fun note(message: String) {
        journal.add("${clock.now()} $message")
    }

    fun advance(code: String) {
        val item = store.find(code) ?: error("dock door assignment $code not found")
        validator.requireValid(item)
        item.moveTo(DockDoorAssignmentStatus.APPROVED)
        store.save(item)
        note("$code is approved")
    }

    fun finish(code: String, reason: String) {
        val item = store.find(code) ?: error("dock door assignment $code not found")
        if (item.isTerminal()) {
            note("$code already closed")
            return
        }
        item.moveTo(DockDoorAssignmentStatus.WITHDRAWN)
        store.save(item)
        note("$code is withdrawn: $reason")
    }

    fun evaluate(code: String): Double {
        val item = store.find(code) ?: error("dock door assignment $code not found")
        val result = engine.run(item)
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

fun newDockDoorAssignmentService(store: DockDoorAssignmentStore, clock: Clock): DockDoorAssignmentService {
    return DockDoorAssignmentService(
        store = store,
        validator = defaultDockDoorAssignmentValidator(),
        clock = clock,
        journal = mutableListOf<String>(),
        engine = defaultDockDoorAssignmentEngine(),
    )
}
