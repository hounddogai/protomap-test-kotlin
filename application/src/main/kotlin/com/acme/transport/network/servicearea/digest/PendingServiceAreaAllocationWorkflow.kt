package com.acme.transport.network.servicearea.digest

import com.acme.transport.kit.flags.Registry
import com.acme.transport.network.clock.Clock

/**
 * Moves pending service area allocations through their life cycle and keeps a journal of what it did.
 */
data class PendingServiceAreaAllocationWorkflow(
    var store: PendingServiceAreaAllocationStore,
    var validator: PendingServiceAreaAllocationValidator,
    var clock: Clock,
    var journal: MutableList<String>,
    var engine: PendingServiceAreaAllocationEngine,
) {
    private fun note(message: String) {
        journal.add("${clock.now()} $message")
    }

    fun advance(code: String) {
        val item = store.find(code) ?: error("pending service area allocation $code not found")
        validator.requireValid(item)
        item.moveTo(PendingServiceAreaAllocationStatus.APPROVED)
        store.save(item)
        note("$code is approved")
    }

    fun complete(code: String, reason: String) {
        val item = store.find(code) ?: error("pending service area allocation $code not found")
        if (item.isTerminal()) {
            note("$code already closed")
            return
        }
        item.moveTo(PendingServiceAreaAllocationStatus.WITHDRAWN)
        store.save(item)
        note("$code is withdrawn: $reason")
    }

    fun evaluate(code: String): Double {
        val item = store.find(code) ?: error("pending service area allocation $code not found")
        val result = engine.compute(item)
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

fun newPendingServiceAreaAllocationWorkflow(store: PendingServiceAreaAllocationStore, clock: Clock, flags: Registry): PendingServiceAreaAllocationWorkflow {
    return PendingServiceAreaAllocationWorkflow(
        store = store,
        validator = defaultPendingServiceAreaAllocationValidator(),
        clock = clock,
        journal = mutableListOf<String>(),
        engine = defaultPendingServiceAreaAllocationEngine(flags),
    )
}
