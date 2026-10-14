package com.acme.transport.terminal.sortation.snapshot

import com.acme.transport.terminal.clock.Clock

/** Moves sortation mappings through their life cycle and keeps a journal of what it did. */
data class SortationMappingWorkflow(
    var store: SortationMappingStore,
    var validator: SortationMappingValidator,
    var clock: Clock,
    var journal: MutableList<String>,
) {
    private fun note(message: String) {
        journal.add("${clock.now()} $message")
    }

    fun open(code: String) {
        val item = store.find(code) ?: error("sortation mapping $code not found")
        validator.requireValid(item)
        item.moveTo(SortationMappingStatus.WARMING)
        store.save(item)
        note("$code is warming")
    }

    fun settle(code: String, reason: String) {
        val item = store.find(code) ?: error("sortation mapping $code not found")
        if (item.isTerminal()) {
            note("$code already closed")
            return
        }
        item.moveTo(SortationMappingStatus.STOPPED)
        store.save(item)
        note("$code is stopped: $reason")
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

fun newSortationMappingWorkflow(store: SortationMappingStore, clock: Clock): SortationMappingWorkflow {
    return SortationMappingWorkflow(
        store = store,
        validator = defaultSortationMappingValidator(),
        clock = clock,
        journal = mutableListOf<String>(),
    )
}
