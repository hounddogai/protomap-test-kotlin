package com.acme.transport.terminal.sortation.snapshot

import com.acme.transport.terminal.clock.Clock

/** Moves sortation overrides through their life cycle and keeps a journal of what it did. */
data class SortationOverrideService(
    var store: SortationOverrideStore,
    var validator: SortationOverrideValidator,
    var clock: Clock,
    var journal: MutableList<String>,
) {
    private fun note(message: String) {
        journal.add("${clock.now()} $message")
    }

    fun open(code: String) {
        val item = store.find(code) ?: error("sortation override $code not found")
        validator.requireValid(item)
        item.moveTo(SortationOverrideStatus.VALIDATED)
        store.save(item)
        note("$code is validated")
    }

    fun close(code: String, reason: String) {
        val item = store.find(code) ?: error("sortation override $code not found")
        if (item.isTerminal()) {
            note("$code already closed")
            return
        }
        item.moveTo(SortationOverrideStatus.RETIRED)
        store.save(item)
        note("$code is retired: $reason")
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

fun newSortationOverrideService(store: SortationOverrideStore, clock: Clock): SortationOverrideService {
    return SortationOverrideService(
        store = store,
        validator = defaultSortationOverrideValidator(),
        clock = clock,
        journal = mutableListOf<String>(),
    )
}
