package com.acme.transport.terminal.sortation.preference

import com.acme.transport.kit.flags.Registry
import com.acme.transport.terminal.clock.Clock

/** Moves sortation allocations through their life cycle and keeps a journal of what it did. */
data class SortationAllocationWorkflow(
    var store: SortationAllocationStore,
    var validator: SortationAllocationValidator,
    var clock: Clock,
    var journal: MutableList<String>,
    var engine: SortationAllocationEngine,
) {
    private fun note(message: String) {
        journal.add("${clock.now()} $message")
    }

    fun submit(code: String) {
        val item = store.find(code) ?: error("sortation allocation $code not found")
        validator.requireValid(item)
        item.moveTo(SortationAllocationStatus.SCHEDULED)
        store.save(item)
        note("$code is scheduled")
    }

    fun complete(code: String, reason: String) {
        val item = store.find(code) ?: error("sortation allocation $code not found")
        if (item.isTerminal()) {
            note("$code already closed")
            return
        }
        item.moveTo(SortationAllocationStatus.CANCELLED)
        store.save(item)
        note("$code is cancelled: $reason")
    }

    fun quote(code: String): Double {
        val item = store.find(code) ?: error("sortation allocation $code not found")
        val result = engine.price(item)
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

fun newSortationAllocationWorkflow(store: SortationAllocationStore, clock: Clock, flags: Registry): SortationAllocationWorkflow {
    return SortationAllocationWorkflow(
        store = store,
        validator = defaultSortationAllocationValidator(),
        clock = clock,
        journal = mutableListOf<String>(),
        engine = defaultSortationAllocationEngine(flags),
    )
}
