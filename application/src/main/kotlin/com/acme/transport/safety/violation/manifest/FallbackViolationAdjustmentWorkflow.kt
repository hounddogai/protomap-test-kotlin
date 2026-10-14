package com.acme.transport.safety.violation.manifest

import com.acme.transport.safety.clock.Clock

/**
 * Moves fallback violation adjustments through their life cycle and keeps a journal of what it did.
 */
data class FallbackViolationAdjustmentCoordinator(
    var store: FallbackViolationAdjustmentStore,
    var validator: FallbackViolationAdjustmentValidator,
    var clock: Clock,
    var journal: MutableList<String>,
    var engine: FallbackViolationAdjustmentEngine,
) {
    private fun note(message: String) {
        journal.add("${clock.now()} $message")
    }

    fun advance(code: String) {
        val item = store.find(code) ?: error("fallback violation adjustment $code not found")
        validator.requireValid(item)
        item.moveTo(FallbackViolationAdjustmentStatus.QUEUED)
        store.save(item)
        note("$code is queued")
    }

    fun settle(code: String, reason: String) {
        val item = store.find(code) ?: error("fallback violation adjustment $code not found")
        if (item.isTerminal()) {
            note("$code already closed")
            return
        }
        item.moveTo(FallbackViolationAdjustmentStatus.FAILED)
        store.save(item)
        note("$code is failed: $reason")
    }

    fun estimate(code: String): Double {
        val item = store.find(code) ?: error("fallback violation adjustment $code not found")
        val result = engine.compute(item)
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

fun newFallbackViolationAdjustmentCoordinator(store: FallbackViolationAdjustmentStore, clock: Clock): FallbackViolationAdjustmentCoordinator {
    return FallbackViolationAdjustmentCoordinator(
        store = store,
        validator = defaultFallbackViolationAdjustmentValidator(),
        clock = clock,
        journal = mutableListOf<String>(),
        engine = defaultFallbackViolationAdjustmentEngine(),
    )
}
