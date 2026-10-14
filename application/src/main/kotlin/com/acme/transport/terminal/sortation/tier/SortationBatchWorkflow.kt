package com.acme.transport.terminal.sortation.tier

import com.acme.transport.kit.flags.Registry
import com.acme.transport.terminal.clock.Clock

/** Moves sortation batches through their life cycle and keeps a journal of what it did. */
data class SortationBatchWorkflow(
    var store: SortationBatchStore,
    var validator: SortationBatchValidator,
    var clock: Clock,
    var journal: MutableList<String>,
    var engine: SortationBatchEngine,
) {
    private fun note(message: String) {
        journal.add("${clock.now()} $message")
    }

    fun open(code: String) {
        val item = store.find(code) ?: error("sortation batch $code not found")
        validator.requireValid(item)
        item.moveTo(SortationBatchStatus.GRANTED)
        store.save(item)
        note("$code is granted")
    }

    fun retire(code: String, reason: String) {
        val item = store.find(code) ?: error("sortation batch $code not found")
        if (item.isTerminal()) {
            note("$code already closed")
            return
        }
        item.moveTo(SortationBatchStatus.EXPIRED)
        store.save(item)
        note("$code is expired: $reason")
    }

    fun score(code: String): Double {
        val item = store.find(code) ?: error("sortation batch $code not found")
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

fun newSortationBatchWorkflow(store: SortationBatchStore, clock: Clock, flags: Registry): SortationBatchWorkflow {
    return SortationBatchWorkflow(
        store = store,
        validator = defaultSortationBatchValidator(),
        clock = clock,
        journal = mutableListOf<String>(),
        engine = defaultSortationBatchEngine(flags),
    )
}
