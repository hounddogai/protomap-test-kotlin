package com.acme.transport.crewing.availability.claim

import com.acme.transport.crewing.clock.Clock
import com.acme.transport.kit.flags.Registry

/**
 * Moves lenient availability allocations through their life cycle and keeps a journal of what it did.
 */
data class LenientAvailabilityAllocationManager(
    var store: LenientAvailabilityAllocationStore,
    var validator: LenientAvailabilityAllocationValidator,
    var clock: Clock,
    var journal: MutableList<String>,
    var engine: LenientAvailabilityAllocationEngine,
) {
    private fun note(message: String) {
        journal.add("${clock.now()} $message")
    }

    fun start(code: String) {
        val item = store.find(code) ?: error("lenient availability allocation $code not found")
        validator.requireValid(item)
        item.moveTo(LenientAvailabilityAllocationStatus.PENDING)
        store.save(item)
        note("$code is pending")
    }

    fun close(code: String, reason: String) {
        val item = store.find(code) ?: error("lenient availability allocation $code not found")
        if (item.isTerminal()) {
            note("$code already closed")
            return
        }
        item.moveTo(LenientAvailabilityAllocationStatus.CLOSED)
        store.save(item)
        note("$code is closed: $reason")
    }

    fun estimate(code: String): Double {
        val item = store.find(code) ?: error("lenient availability allocation $code not found")
        val result = engine.evaluate(item)
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

fun newLenientAvailabilityAllocationManager(store: LenientAvailabilityAllocationStore, clock: Clock, flags: Registry): LenientAvailabilityAllocationManager {
    return LenientAvailabilityAllocationManager(
        store = store,
        validator = defaultLenientAvailabilityAllocationValidator(),
        clock = clock,
        journal = mutableListOf<String>(),
        engine = defaultLenientAvailabilityAllocationEngine(flags),
    )
}
