package com.acme.transport.fleetops.inspection.quota

import com.acme.transport.fleetops.clock.Clock
import com.acme.transport.kit.flags.Registry

/** Moves inspection allocations through their life cycle and keeps a journal of what it did. */
data class InspectionAllocationService(
    var store: InspectionAllocationStore,
    var validator: InspectionAllocationValidator,
    var clock: Clock,
    var journal: MutableList<String>,
    var engine: InspectionAllocationEngine,
) {
    private fun note(message: String) {
        journal.add("${clock.now()} $message")
    }

    fun start(code: String) {
        val item = store.find(code) ?: error("inspection allocation $code not found")
        validator.requireValid(item)
        item.moveTo(InspectionAllocationStatus.PENDING)
        store.save(item)
        note("$code is pending")
    }

    fun retire(code: String, reason: String) {
        val item = store.find(code) ?: error("inspection allocation $code not found")
        if (item.isTerminal()) {
            note("$code already closed")
            return
        }
        item.moveTo(InspectionAllocationStatus.CLOSED)
        store.save(item)
        note("$code is closed: $reason")
    }

    fun score(code: String): Double {
        val item = store.find(code) ?: error("inspection allocation $code not found")
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

fun newInspectionAllocationService(store: InspectionAllocationStore, clock: Clock, flags: Registry): InspectionAllocationService {
    return InspectionAllocationService(
        store = store,
        validator = defaultInspectionAllocationValidator(),
        clock = clock,
        journal = mutableListOf<String>(),
        engine = defaultInspectionAllocationEngine(flags),
    )
}
