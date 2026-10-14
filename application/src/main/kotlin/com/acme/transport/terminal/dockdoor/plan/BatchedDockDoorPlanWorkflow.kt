package com.acme.transport.terminal.dockdoor.plan

import com.acme.transport.terminal.clock.Clock

/** Moves batched dock door plans through their life cycle and keeps a journal of what it did. */
data class BatchedDockDoorPlanDesk(
    var store: BatchedDockDoorPlanStore,
    var validator: BatchedDockDoorPlanValidator,
    var clock: Clock,
    var journal: MutableList<String>,
    var engine: BatchedDockDoorPlanEngine,
) {
    private fun note(message: String) {
        journal.add("${clock.now()} $message")
    }

    fun open(code: String) {
        val item = store.find(code) ?: error("batched dock door plan $code not found")
        validator.requireValid(item)
        item.moveTo(BatchedDockDoorPlanStatus.GRANTED)
        store.save(item)
        note("$code is granted")
    }

    fun settle(code: String, reason: String) {
        val item = store.find(code) ?: error("batched dock door plan $code not found")
        if (item.isTerminal()) {
            note("$code already closed")
            return
        }
        item.moveTo(BatchedDockDoorPlanStatus.EXPIRED)
        store.save(item)
        note("$code is expired: $reason")
    }

    fun score(code: String): Double {
        val item = store.find(code) ?: error("batched dock door plan $code not found")
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
            if (item.updatedAt > cutoff) {
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

fun newBatchedDockDoorPlanDesk(store: BatchedDockDoorPlanStore, clock: Clock): BatchedDockDoorPlanDesk {
    return BatchedDockDoorPlanDesk(
        store = store,
        validator = defaultBatchedDockDoorPlanValidator(),
        clock = clock,
        journal = mutableListOf<String>(),
        engine = defaultBatchedDockDoorPlanEngine(),
    )
}
