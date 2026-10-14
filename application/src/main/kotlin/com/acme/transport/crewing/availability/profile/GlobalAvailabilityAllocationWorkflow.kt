package com.acme.transport.crewing.availability.profile

import com.acme.transport.crewing.clock.Clock
import com.acme.transport.kit.flags.Registry

/**
 * Moves global availability allocations through their life cycle and keeps a journal of what it did.
 */
data class GlobalAvailabilityAllocationDesk(
    var store: GlobalAvailabilityAllocationStore,
    var validator: GlobalAvailabilityAllocationValidator,
    var clock: Clock,
    var journal: MutableList<String>,
    var engine: GlobalAvailabilityAllocationEngine,
) {
    private fun note(message: String) {
        journal.add("${clock.now()} $message")
    }

    fun open(code: String) {
        val item = store.find(code) ?: error("global availability allocation $code not found")
        validator.requireValid(item)
        item.moveTo(GlobalAvailabilityAllocationStatus.SCHEDULED)
        store.save(item)
        note("$code is scheduled")
    }

    fun retire(code: String, reason: String) {
        val item = store.find(code) ?: error("global availability allocation $code not found")
        if (item.isTerminal()) {
            note("$code already closed")
            return
        }
        item.moveTo(GlobalAvailabilityAllocationStatus.CANCELLED)
        store.save(item)
        note("$code is cancelled: $reason")
    }

    fun score(code: String): Double {
        val item = store.find(code) ?: error("global availability allocation $code not found")
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

fun newGlobalAvailabilityAllocationDesk(store: GlobalAvailabilityAllocationStore, clock: Clock, flags: Registry): GlobalAvailabilityAllocationDesk {
    return GlobalAvailabilityAllocationDesk(
        store = store,
        validator = defaultGlobalAvailabilityAllocationValidator(),
        clock = clock,
        journal = mutableListOf<String>(),
        engine = defaultGlobalAvailabilityAllocationEngine(flags),
    )
}
