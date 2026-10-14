package com.acme.transport.network.cutoff.allocation

import com.acme.transport.kit.flags.Registry
import com.acme.transport.network.clock.Clock

/** Moves cutoff adjustments through their life cycle and keeps a journal of what it did. */
data class CutoffAdjustmentWorkflow(
    var store: CutoffAdjustmentStore,
    var validator: CutoffAdjustmentValidator,
    var clock: Clock,
    var journal: MutableList<String>,
    var engine: CutoffAdjustmentEngine,
) {
    private fun note(message: String) {
        journal.add("${clock.now()} $message")
    }

    fun start(code: String) {
        val item = store.find(code) ?: error("cutoff adjustment $code not found")
        validator.requireValid(item)
        item.moveTo(CutoffAdjustmentStatus.QUEUED)
        store.save(item)
        note("$code is queued")
    }

    fun close(code: String, reason: String) {
        val item = store.find(code) ?: error("cutoff adjustment $code not found")
        if (item.isTerminal()) {
            note("$code already closed")
            return
        }
        item.moveTo(CutoffAdjustmentStatus.FAILED)
        store.save(item)
        note("$code is failed: $reason")
    }

    fun estimate(code: String): Double {
        val item = store.find(code) ?: error("cutoff adjustment $code not found")
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

fun newCutoffAdjustmentWorkflow(store: CutoffAdjustmentStore, clock: Clock, flags: Registry): CutoffAdjustmentWorkflow {
    return CutoffAdjustmentWorkflow(
        store = store,
        validator = defaultCutoffAdjustmentValidator(),
        clock = clock,
        journal = mutableListOf<String>(),
        engine = defaultCutoffAdjustmentEngine(flags),
    )
}
