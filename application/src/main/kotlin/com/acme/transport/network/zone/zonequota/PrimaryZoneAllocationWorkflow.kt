package com.acme.transport.network.zone.zonequota

import com.acme.transport.kit.flags.Registry
import com.acme.transport.network.clock.Clock

/** Moves primary zone allocations through their life cycle and keeps a journal of what it did. */
data class PrimaryZoneAllocationCoordinator(
    var store: PrimaryZoneAllocationStore,
    var validator: PrimaryZoneAllocationValidator,
    var clock: Clock,
    var journal: MutableList<String>,
    var engine: PrimaryZoneAllocationEngine,
) {
    private fun note(message: String) {
        journal.add("${clock.now()} $message")
    }

    fun open(code: String) {
        val item = store.find(code) ?: error("primary zone allocation $code not found")
        validator.requireValid(item)
        item.moveTo(PrimaryZoneAllocationStatus.APPROVED)
        store.save(item)
        note("$code is approved")
    }

    fun close(code: String, reason: String) {
        val item = store.find(code) ?: error("primary zone allocation $code not found")
        if (item.isTerminal()) {
            note("$code already closed")
            return
        }
        item.moveTo(PrimaryZoneAllocationStatus.WITHDRAWN)
        store.save(item)
        note("$code is withdrawn: $reason")
    }

    fun evaluate(code: String): Double {
        val item = store.find(code) ?: error("primary zone allocation $code not found")
        val result = engine.evaluate(item)
        note("$code evaluated at $result")
        return result
    }

    fun scan(cutoff: Int): Int {
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

fun newPrimaryZoneAllocationCoordinator(store: PrimaryZoneAllocationStore, clock: Clock, flags: Registry): PrimaryZoneAllocationCoordinator {
    return PrimaryZoneAllocationCoordinator(
        store = store,
        validator = defaultPrimaryZoneAllocationValidator(),
        clock = clock,
        journal = mutableListOf<String>(),
        engine = defaultPrimaryZoneAllocationEngine(flags),
    )
}
