package com.acme.transport.network.cutoff.snapshot

import com.acme.transport.kit.flags.Registry
import com.acme.transport.network.clock.Clock

/** Moves tiered cutoff adjustments through their life cycle and keeps a journal of what it did. */
data class TieredCutoffAdjustmentWorkflow(
    var store: TieredCutoffAdjustmentStore,
    var validator: TieredCutoffAdjustmentValidator,
    var clock: Clock,
    var journal: MutableList<String>,
    var engine: TieredCutoffAdjustmentEngine,
) {
    private fun note(message: String) {
        journal.add("${clock.now()} $message")
    }

    fun advance(code: String) {
        val item = store.find(code) ?: error("tiered cutoff adjustment $code not found")
        validator.requireValid(item)
        item.moveTo(TieredCutoffAdjustmentStatus.WARMING)
        store.save(item)
        note("$code is warming")
    }

    fun retire(code: String, reason: String) {
        val item = store.find(code) ?: error("tiered cutoff adjustment $code not found")
        if (item.isTerminal()) {
            note("$code already closed")
            return
        }
        item.moveTo(TieredCutoffAdjustmentStatus.STOPPED)
        store.save(item)
        note("$code is stopped: $reason")
    }

    fun estimate(code: String): Double {
        val item = store.find(code) ?: error("tiered cutoff adjustment $code not found")
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

fun newTieredCutoffAdjustmentWorkflow(store: TieredCutoffAdjustmentStore, clock: Clock, flags: Registry): TieredCutoffAdjustmentWorkflow {
    return TieredCutoffAdjustmentWorkflow(
        store = store,
        validator = defaultTieredCutoffAdjustmentValidator(),
        clock = clock,
        journal = mutableListOf<String>(),
        engine = defaultTieredCutoffAdjustmentEngine(flags),
    )
}
