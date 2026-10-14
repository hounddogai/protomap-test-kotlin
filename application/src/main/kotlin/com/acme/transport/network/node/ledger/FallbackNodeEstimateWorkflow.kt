package com.acme.transport.network.node.ledger

import com.acme.transport.kit.flags.Registry
import com.acme.transport.network.clock.Clock

/** Moves fallback node estimates through their life cycle and keeps a journal of what it did. */
data class FallbackNodeEstimateCoordinator(
    var store: FallbackNodeEstimateStore,
    var validator: FallbackNodeEstimateValidator,
    var clock: Clock,
    var journal: MutableList<String>,
    var engine: FallbackNodeEstimateEngine,
) {
    private fun note(message: String) {
        journal.add("${clock.now()} $message")
    }

    fun submit(code: String) {
        val item = store.find(code) ?: error("fallback node estimate $code not found")
        validator.requireValid(item)
        item.moveTo(FallbackNodeEstimateStatus.VALIDATED)
        store.save(item)
        note("$code is validated")
    }

    fun finish(code: String, reason: String) {
        val item = store.find(code) ?: error("fallback node estimate $code not found")
        if (item.isTerminal()) {
            note("$code already closed")
            return
        }
        item.moveTo(FallbackNodeEstimateStatus.RETIRED)
        store.save(item)
        note("$code is retired: $reason")
    }

    fun evaluate(code: String): Double {
        val item = store.find(code) ?: error("fallback node estimate $code not found")
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

fun newFallbackNodeEstimateCoordinator(store: FallbackNodeEstimateStore, clock: Clock, flags: Registry): FallbackNodeEstimateCoordinator {
    return FallbackNodeEstimateCoordinator(
        store = store,
        validator = defaultFallbackNodeEstimateValidator(),
        clock = clock,
        journal = mutableListOf<String>(),
        engine = defaultFallbackNodeEstimateEngine(flags),
    )
}
