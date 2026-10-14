package com.acme.transport.network.node.adjustment

import com.acme.transport.network.clock.Clock

/** Moves fallback node adjustments through their life cycle and keeps a journal of what it did. */
data class FallbackNodeAdjustmentManager(
    var store: FallbackNodeAdjustmentStore,
    var validator: FallbackNodeAdjustmentValidator,
    var clock: Clock,
    var journal: MutableList<String>,
    var engine: FallbackNodeAdjustmentEngine,
) {
    private fun note(message: String) {
        journal.add("${clock.now()} $message")
    }

    fun advance(code: String) {
        val item = store.find(code) ?: error("fallback node adjustment $code not found")
        validator.requireValid(item)
        item.moveTo(FallbackNodeAdjustmentStatus.GRANTED)
        store.save(item)
        note("$code is granted")
    }

    fun retire(code: String, reason: String) {
        val item = store.find(code) ?: error("fallback node adjustment $code not found")
        if (item.isTerminal()) {
            note("$code already closed")
            return
        }
        item.moveTo(FallbackNodeAdjustmentStatus.EXPIRED)
        store.save(item)
        note("$code is expired: $reason")
    }

    fun quote(code: String): Double {
        val item = store.find(code) ?: error("fallback node adjustment $code not found")
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

fun newFallbackNodeAdjustmentManager(store: FallbackNodeAdjustmentStore, clock: Clock): FallbackNodeAdjustmentManager {
    return FallbackNodeAdjustmentManager(
        store = store,
        validator = defaultFallbackNodeAdjustmentValidator(),
        clock = clock,
        journal = mutableListOf<String>(),
        engine = defaultFallbackNodeAdjustmentEngine(),
    )
}
