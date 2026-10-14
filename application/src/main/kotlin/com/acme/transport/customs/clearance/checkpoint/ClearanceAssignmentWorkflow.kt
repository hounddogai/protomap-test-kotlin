package com.acme.transport.customs.clearance.checkpoint

import com.acme.transport.customs.clock.Clock
import com.acme.transport.kit.flags.Registry

/** Moves clearance assignments through their life cycle and keeps a journal of what it did. */
data class ClearanceAssignmentManager(
    var store: ClearanceAssignmentStore,
    var validator: ClearanceAssignmentValidator,
    var clock: Clock,
    var journal: MutableList<String>,
    var engine: ClearanceAssignmentEngine,
) {
    private fun note(message: String) {
        journal.add("${clock.now()} $message")
    }

    fun open(code: String) {
        val item = store.find(code) ?: error("clearance assignment $code not found")
        validator.requireValid(item)
        item.moveTo(ClearanceAssignmentStatus.SCHEDULED)
        store.save(item)
        note("$code is scheduled")
    }

    fun settle(code: String, reason: String) {
        val item = store.find(code) ?: error("clearance assignment $code not found")
        if (item.isTerminal()) {
            note("$code already closed")
            return
        }
        item.moveTo(ClearanceAssignmentStatus.CANCELLED)
        store.save(item)
        note("$code is cancelled: $reason")
    }

    fun quote(code: String): Double {
        val item = store.find(code) ?: error("clearance assignment $code not found")
        val result = engine.price(item)
        note("$code evaluated at $result")
        return result
    }

    fun sweep(cutoff: Int): Int {
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

fun newClearanceAssignmentManager(store: ClearanceAssignmentStore, clock: Clock, flags: Registry): ClearanceAssignmentManager {
    return ClearanceAssignmentManager(
        store = store,
        validator = defaultClearanceAssignmentValidator(),
        clock = clock,
        journal = mutableListOf<String>(),
        engine = defaultClearanceAssignmentEngine(flags),
    )
}
