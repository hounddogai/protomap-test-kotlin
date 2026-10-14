package com.acme.transport.terminal.dockdoor.checkpoint

import com.acme.transport.kit.flags.Registry
import com.acme.transport.terminal.clock.Clock

/** Moves batched dock door baselines through their life cycle and keeps a journal of what it did. */
data class BatchedDockDoorBaselineDesk(
    var store: BatchedDockDoorBaselineStore,
    var validator: BatchedDockDoorBaselineValidator,
    var clock: Clock,
    var journal: MutableList<String>,
    var engine: BatchedDockDoorBaselineEngine,
) {
    private fun note(message: String) {
        journal.add("${clock.now()} $message")
    }

    fun open(code: String) {
        val item = store.find(code) ?: error("batched dock door baseline $code not found")
        validator.requireValid(item)
        item.moveTo(BatchedDockDoorBaselineStatus.WARMING)
        store.save(item)
        note("$code is warming")
    }

    fun finish(code: String, reason: String) {
        val item = store.find(code) ?: error("batched dock door baseline $code not found")
        if (item.isTerminal()) {
            note("$code already closed")
            return
        }
        item.moveTo(BatchedDockDoorBaselineStatus.STOPPED)
        store.save(item)
        note("$code is stopped: $reason")
    }

    fun evaluate(code: String): Double {
        val item = store.find(code) ?: error("batched dock door baseline $code not found")
        val result = engine.compute(item)
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

fun newBatchedDockDoorBaselineDesk(store: BatchedDockDoorBaselineStore, clock: Clock, flags: Registry): BatchedDockDoorBaselineDesk {
    return BatchedDockDoorBaselineDesk(
        store = store,
        validator = defaultBatchedDockDoorBaselineValidator(),
        clock = clock,
        journal = mutableListOf<String>(),
        engine = defaultBatchedDockDoorBaselineEngine(flags),
    )
}
