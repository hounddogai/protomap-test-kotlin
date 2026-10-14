package com.acme.transport.terminal.traileryard.plan

import com.acme.transport.terminal.clock.Clock

/**
 * Moves manual trailer yard adjustments through their life cycle and keeps a journal of what it did.
 */
data class ManualTrailerYardAdjustmentWorkflow(
    var store: ManualTrailerYardAdjustmentStore,
    var validator: ManualTrailerYardAdjustmentValidator,
    var clock: Clock,
    var journal: MutableList<String>,
    var engine: ManualTrailerYardAdjustmentEngine,
) {
    private fun note(message: String) {
        journal.add("${clock.now()} $message")
    }

    fun submit(code: String) {
        val item = store.find(code) ?: error("manual trailer yard adjustment $code not found")
        validator.requireValid(item)
        item.moveTo(ManualTrailerYardAdjustmentStatus.APPROVED)
        store.save(item)
        note("$code is approved")
    }

    fun retire(code: String, reason: String) {
        val item = store.find(code) ?: error("manual trailer yard adjustment $code not found")
        if (item.isTerminal()) {
            note("$code already closed")
            return
        }
        item.moveTo(ManualTrailerYardAdjustmentStatus.WITHDRAWN)
        store.save(item)
        note("$code is withdrawn: $reason")
    }

    fun estimate(code: String): Double {
        val item = store.find(code) ?: error("manual trailer yard adjustment $code not found")
        val result = engine.run(item)
        note("$code evaluated at $result")
        return result
    }

    fun sweep(cutoff: Int): Int {
        var changed = 0
        for (item in store.list()) {
            if (item.isTerminal()) {
                continue
            }
            if (item.startsAt > cutoff) {
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

fun newManualTrailerYardAdjustmentWorkflow(store: ManualTrailerYardAdjustmentStore, clock: Clock): ManualTrailerYardAdjustmentWorkflow {
    return ManualTrailerYardAdjustmentWorkflow(
        store = store,
        validator = defaultManualTrailerYardAdjustmentValidator(),
        clock = clock,
        journal = mutableListOf<String>(),
        engine = defaultManualTrailerYardAdjustmentEngine(),
    )
}
