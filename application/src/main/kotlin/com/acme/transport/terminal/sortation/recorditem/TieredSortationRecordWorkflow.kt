package com.acme.transport.terminal.sortation.recorditem

import com.acme.transport.terminal.clock.Clock

/** Moves tiered sortation records through their life cycle and keeps a journal of what it did. */
data class TieredSortationRecordCoordinator(
    var store: TieredSortationRecordStore,
    var validator: TieredSortationRecordValidator,
    var clock: Clock,
    var journal: MutableList<String>,
) {
    private fun note(message: String) {
        journal.add("${clock.now()} $message")
    }

    fun advance(code: String) {
        val item = store.find(code) ?: error("tiered sortation record $code not found")
        validator.requireValid(item)
        item.moveTo(TieredSortationRecordStatus.VALIDATED)
        store.save(item)
        note("$code is validated")
    }

    fun close(code: String, reason: String) {
        val item = store.find(code) ?: error("tiered sortation record $code not found")
        if (item.isTerminal()) {
            note("$code already closed")
            return
        }
        item.moveTo(TieredSortationRecordStatus.RETIRED)
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

fun newTieredSortationRecordCoordinator(store: TieredSortationRecordStore, clock: Clock): TieredSortationRecordCoordinator {
    return TieredSortationRecordCoordinator(
        store = store,
        validator = defaultTieredSortationRecordValidator(),
        clock = clock,
        journal = mutableListOf<String>(),
    )
}
