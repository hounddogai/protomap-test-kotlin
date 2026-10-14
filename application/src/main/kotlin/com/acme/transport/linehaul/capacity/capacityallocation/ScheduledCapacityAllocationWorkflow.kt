package com.acme.transport.linehaul.capacity.capacityallocation

import com.acme.transport.kit.flags.Registry
import com.acme.transport.linehaul.clock.Clock

/**
 * Moves scheduled capacity allocations through their life cycle and keeps a journal of what it did.
 */
data class ScheduledCapacityAllocationCoordinator(
    var store: ScheduledCapacityAllocationStore,
    var validator: ScheduledCapacityAllocationValidator,
    var clock: Clock,
    var journal: MutableList<String>,
    var engine: ScheduledCapacityAllocationEngine,
) {
    private fun note(message: String) {
        journal.add("${clock.now()} $message")
    }

    fun open(code: String) {
        val item = store.find(code) ?: error("scheduled capacity allocation $code not found")
        validator.requireValid(item)
        item.moveTo(ScheduledCapacityAllocationStatus.VALIDATED)
        store.save(item)
        note("$code is validated")
    }

    fun complete(code: String, reason: String) {
        val item = store.find(code) ?: error("scheduled capacity allocation $code not found")
        if (item.isTerminal()) {
            note("$code already closed")
            return
        }
        item.moveTo(ScheduledCapacityAllocationStatus.RETIRED)
        store.save(item)
        note("$code is retired: $reason")
    }

    fun quote(code: String): Double {
        val item = store.find(code) ?: error("scheduled capacity allocation $code not found")
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

fun newScheduledCapacityAllocationCoordinator(store: ScheduledCapacityAllocationStore, clock: Clock, flags: Registry): ScheduledCapacityAllocationCoordinator {
    return ScheduledCapacityAllocationCoordinator(
        store = store,
        validator = defaultScheduledCapacityAllocationValidator(),
        clock = clock,
        journal = mutableListOf<String>(),
        engine = defaultScheduledCapacityAllocationEngine(flags),
    )
}
