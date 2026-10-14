package com.acme.transport.crewing.swap.claim

import com.acme.transport.crewing.clock.Clock
import com.acme.transport.kit.flags.Registry

/** Moves swap claims through their life cycle and keeps a journal of what it did. */
data class SwapClaimManager(
    var store: SwapClaimStore,
    var validator: SwapClaimValidator,
    var clock: Clock,
    var journal: MutableList<String>,
    var engine: SwapClaimEngine,
) {
    private fun note(message: String) {
        journal.add("${clock.now()} $message")
    }

    fun open(code: String) {
        val item = store.find(code) ?: error("swap claim $code not found")
        validator.requireValid(item)
        item.moveTo(SwapClaimStatus.QUEUED)
        store.save(item)
        note("$code is queued")
    }

    fun finish(code: String, reason: String) {
        val item = store.find(code) ?: error("swap claim $code not found")
        if (item.isTerminal()) {
            note("$code already closed")
            return
        }
        item.moveTo(SwapClaimStatus.FAILED)
        store.save(item)
        note("$code is failed: $reason")
    }

    fun estimate(code: String): Double {
        val item = store.find(code) ?: error("swap claim $code not found")
        val result = engine.price(item)
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

fun newSwapClaimManager(store: SwapClaimStore, clock: Clock, flags: Registry): SwapClaimManager {
    return SwapClaimManager(
        store = store,
        validator = defaultSwapClaimValidator(),
        clock = clock,
        journal = mutableListOf<String>(),
        engine = defaultSwapClaimEngine(flags),
    )
}
