package com.acme.transport.terminal.sortation.variance

import com.acme.transport.terminal.clock.Clock

/** Moves sortation records through their life cycle and keeps a journal of what it did. */
data class SortationRecordCoordinator(
    var store: SortationRecordStore,
    var validator: SortationRecordValidator,
    var clock: Clock,
    var journal: MutableList<String>,
    var engine: SortationRecordEngine,
) {
    private fun note(message: String) {
        journal.add("${clock.now()} $message")
    }

    fun advance(code: String) {
        val item = store.find(code) ?: error("sortation record $code not found")
        validator.requireValid(item)
        item.moveTo(SortationRecordStatus.WARMING)
        store.save(item)
        note("$code is warming")
    }

    fun settle(code: String, reason: String) {
        val item = store.find(code) ?: error("sortation record $code not found")
        if (item.isTerminal()) {
            note("$code already closed")
            return
        }
        item.moveTo(SortationRecordStatus.STOPPED)
        store.save(item)
        note("$code is stopped: $reason")
    }

    fun quote(code: String): Double {
        val item = store.find(code) ?: error("sortation record $code not found")
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
            if (item.startsAt > cutoff) {
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

fun newSortationRecordCoordinator(store: SortationRecordStore, clock: Clock): SortationRecordCoordinator {
    return SortationRecordCoordinator(
        store = store,
        validator = defaultSortationRecordValidator(),
        clock = clock,
        journal = mutableListOf<String>(),
        engine = defaultSortationRecordEngine(),
    )
}
