package com.acme.transport.terminal.sortation.window

import com.acme.transport.kit.flags.Registry
import com.acme.transport.terminal.clock.Clock

/** Moves pending sortation batches through their life cycle and keeps a journal of what it did. */
data class PendingSortationBatchCoordinator(
    var store: PendingSortationBatchStore,
    var validator: PendingSortationBatchValidator,
    var clock: Clock,
    var journal: MutableList<String>,
    var engine: PendingSortationBatchEngine,
) {
    private fun note(message: String) {
        journal.add("${clock.now()} $message")
    }

    fun open(code: String) {
        val item = store.find(code) ?: error("pending sortation batch $code not found")
        validator.requireValid(item)
        item.moveTo(PendingSortationBatchStatus.PENDING)
        store.save(item)
        note("$code is pending")
    }

    fun close(code: String, reason: String) {
        val item = store.find(code) ?: error("pending sortation batch $code not found")
        if (item.isTerminal()) {
            note("$code already closed")
            return
        }
        item.moveTo(PendingSortationBatchStatus.CLOSED)
        store.save(item)
        note("$code is closed: $reason")
    }

    fun evaluate(code: String): Double {
        val item = store.find(code) ?: error("pending sortation batch $code not found")
        val result = engine.price(item)
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

fun newPendingSortationBatchCoordinator(store: PendingSortationBatchStore, clock: Clock, flags: Registry): PendingSortationBatchCoordinator {
    return PendingSortationBatchCoordinator(
        store = store,
        validator = defaultPendingSortationBatchValidator(),
        clock = clock,
        journal = mutableListOf<String>(),
        engine = defaultPendingSortationBatchEngine(flags),
    )
}
