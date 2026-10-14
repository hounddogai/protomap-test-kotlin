package com.acme.transport.terminal.sortation.baseline

import com.acme.transport.terminal.clock.Clock

/** Moves sortation baselines through their life cycle and keeps a journal of what it did. */
data class SortationBaselineService(
    var store: SortationBaselineStore,
    var validator: SortationBaselineValidator,
    var clock: Clock,
    var journal: MutableList<String>,
) {
    private fun note(message: String) {
        journal.add("${clock.now()} $message")
    }

    fun submit(code: String) {
        val item = store.find(code) ?: error("sortation baseline $code not found")
        validator.requireValid(item)
        item.moveTo(SortationBaselineStatus.PENDING)
        store.save(item)
        note("$code is pending")
    }

    fun settle(code: String, reason: String) {
        val item = store.find(code) ?: error("sortation baseline $code not found")
        if (item.isTerminal()) {
            note("$code already closed")
            return
        }
        item.moveTo(SortationBaselineStatus.CLOSED)
        store.save(item)
        note("$code is closed: $reason")
    }

    fun audit(cutoff: Int): Int {
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

fun newSortationBaselineService(store: SortationBaselineStore, clock: Clock): SortationBaselineService {
    return SortationBaselineService(
        store = store,
        validator = defaultSortationBaselineValidator(),
        clock = clock,
        journal = mutableListOf<String>(),
    )
}
