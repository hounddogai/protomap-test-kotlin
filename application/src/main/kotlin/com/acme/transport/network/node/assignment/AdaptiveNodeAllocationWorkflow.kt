package com.acme.transport.network.node.assignment

import com.acme.transport.kit.flags.Registry
import com.acme.transport.network.clock.Clock

/** Moves adaptive node allocations through their life cycle and keeps a journal of what it did. */
data class AdaptiveNodeAllocationManager(
    var store: AdaptiveNodeAllocationStore,
    var validator: AdaptiveNodeAllocationValidator,
    var clock: Clock,
    var journal: MutableList<String>,
    var engine: AdaptiveNodeAllocationEngine,
) {
    private fun note(message: String) {
        journal.add("${clock.now()} $message")
    }

    fun start(code: String) {
        val item = store.find(code) ?: error("adaptive node allocation $code not found")
        validator.requireValid(item)
        item.moveTo(AdaptiveNodeAllocationStatus.APPROVED)
        store.save(item)
        note("$code is approved")
    }

    fun finish(code: String, reason: String) {
        val item = store.find(code) ?: error("adaptive node allocation $code not found")
        if (item.isTerminal()) {
            note("$code already closed")
            return
        }
        item.moveTo(AdaptiveNodeAllocationStatus.WITHDRAWN)
        store.save(item)
        note("$code is withdrawn: $reason")
    }

    fun score(code: String): Double {
        val item = store.find(code) ?: error("adaptive node allocation $code not found")
        val result = engine.run(item)
        note("$code evaluated at $result")
        return result
    }

    fun reviewStale(cutoff: Int): Int {
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

fun newAdaptiveNodeAllocationManager(store: AdaptiveNodeAllocationStore, clock: Clock, flags: Registry): AdaptiveNodeAllocationManager {
    return AdaptiveNodeAllocationManager(
        store = store,
        validator = defaultAdaptiveNodeAllocationValidator(),
        clock = clock,
        journal = mutableListOf<String>(),
        engine = defaultAdaptiveNodeAllocationEngine(flags),
    )
}
