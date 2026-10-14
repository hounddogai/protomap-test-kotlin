package com.acme.transport.crewing.swap.assignment

import com.acme.transport.crewing.clock.Clock
import com.acme.transport.kit.flags.Registry

/** Moves adaptive swap allocations through their life cycle and keeps a journal of what it did. */
data class AdaptiveSwapAllocationService(
    var store: AdaptiveSwapAllocationStore,
    var validator: AdaptiveSwapAllocationValidator,
    var clock: Clock,
    var journal: MutableList<String>,
    var engine: AdaptiveSwapAllocationEngine,
) {
    private fun note(message: String) {
        journal.add("${clock.now()} $message")
    }

    fun start(code: String) {
        val item = store.find(code) ?: error("adaptive swap allocation $code not found")
        validator.requireValid(item)
        item.moveTo(AdaptiveSwapAllocationStatus.PENDING)
        store.save(item)
        note("$code is pending")
    }

    fun finish(code: String, reason: String) {
        val item = store.find(code) ?: error("adaptive swap allocation $code not found")
        if (item.isTerminal()) {
            note("$code already closed")
            return
        }
        item.moveTo(AdaptiveSwapAllocationStatus.CLOSED)
        store.save(item)
        note("$code is closed: $reason")
    }

    fun score(code: String): Double {
        val item = store.find(code) ?: error("adaptive swap allocation $code not found")
        val result = engine.evaluate(item)
        note("$code evaluated at $result")
        return result
    }

    fun sweep(cutoff: Int): Int {
        var changed = 0
        for (item in store.list()) {
            if (item.isTerminal()) {
                continue
            }
            if (item.createdAt > cutoff) {
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

fun newAdaptiveSwapAllocationService(store: AdaptiveSwapAllocationStore, clock: Clock, flags: Registry): AdaptiveSwapAllocationService {
    return AdaptiveSwapAllocationService(
        store = store,
        validator = defaultAdaptiveSwapAllocationValidator(),
        clock = clock,
        journal = mutableListOf<String>(),
        engine = defaultAdaptiveSwapAllocationEngine(flags),
    )
}
