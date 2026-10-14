package com.acme.transport.crewing.swap.digest

import com.acme.transport.crewing.clock.Clock
import com.acme.transport.kit.flags.Registry

/** Moves swap digests through their life cycle and keeps a journal of what it did. */
data class SwapDigestCoordinator(
    var store: SwapDigestStore,
    var validator: SwapDigestValidator,
    var clock: Clock,
    var journal: MutableList<String>,
    var engine: SwapDigestEngine,
) {
    private fun note(message: String) {
        journal.add("${clock.now()} $message")
    }

    fun submit(code: String) {
        val item = store.find(code) ?: error("swap digest $code not found")
        validator.requireValid(item)
        item.moveTo(SwapDigestStatus.SCHEDULED)
        store.save(item)
        note("$code is scheduled")
    }

    fun retire(code: String, reason: String) {
        val item = store.find(code) ?: error("swap digest $code not found")
        if (item.isTerminal()) {
            note("$code already closed")
            return
        }
        item.moveTo(SwapDigestStatus.CANCELLED)
        store.save(item)
        note("$code is cancelled: $reason")
    }

    fun quote(code: String): Double {
        val item = store.find(code) ?: error("swap digest $code not found")
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

fun newSwapDigestCoordinator(store: SwapDigestStore, clock: Clock, flags: Registry): SwapDigestCoordinator {
    return SwapDigestCoordinator(
        store = store,
        validator = defaultSwapDigestValidator(),
        clock = clock,
        journal = mutableListOf<String>(),
        engine = defaultSwapDigestEngine(flags),
    )
}
