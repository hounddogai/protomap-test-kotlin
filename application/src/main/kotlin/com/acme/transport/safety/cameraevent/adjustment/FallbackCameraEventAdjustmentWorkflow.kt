package com.acme.transport.safety.cameraevent.adjustment

import com.acme.transport.safety.clock.Clock

/**
 * Moves fallback camera event adjustments through their life cycle and keeps a journal of what it did.
 */
data class FallbackCameraEventAdjustmentCoordinator(
    var store: FallbackCameraEventAdjustmentStore,
    var validator: FallbackCameraEventAdjustmentValidator,
    var clock: Clock,
    var journal: MutableList<String>,
    var engine: FallbackCameraEventAdjustmentEngine,
) {
    private fun note(message: String) {
        journal.add("${clock.now()} $message")
    }

    fun open(code: String) {
        val item = store.find(code) ?: error("fallback camera event adjustment $code not found")
        validator.requireValid(item)
        item.moveTo(FallbackCameraEventAdjustmentStatus.IN_REVIEW)
        store.save(item)
        note("$code is in review")
    }

    fun retire(code: String, reason: String) {
        val item = store.find(code) ?: error("fallback camera event adjustment $code not found")
        if (item.isTerminal()) {
            note("$code already closed")
            return
        }
        item.moveTo(FallbackCameraEventAdjustmentStatus.ARCHIVED)
        store.save(item)
        note("$code is archived: $reason")
    }

    fun estimate(code: String): Double {
        val item = store.find(code) ?: error("fallback camera event adjustment $code not found")
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

fun newFallbackCameraEventAdjustmentCoordinator(store: FallbackCameraEventAdjustmentStore, clock: Clock): FallbackCameraEventAdjustmentCoordinator {
    return FallbackCameraEventAdjustmentCoordinator(
        store = store,
        validator = defaultFallbackCameraEventAdjustmentValidator(),
        clock = clock,
        journal = mutableListOf<String>(),
        engine = defaultFallbackCameraEventAdjustmentEngine(),
    )
}
