package com.acme.transport.terminal.sortation.group

import com.acme.transport.terminal.clock.Clock

/** Moves capped sortation estimates through their life cycle and keeps a journal of what it did. */
data class CappedSortationEstimateManager(
    var store: CappedSortationEstimateStore,
    var validator: CappedSortationEstimateValidator,
    var clock: Clock,
    var journal: MutableList<String>,
) {
    private fun note(message: String) {
        journal.add("${clock.now()} $message")
    }

    fun advance(code: String) {
        val item = store.find(code) ?: error("capped sortation estimate $code not found")
        validator.requireValid(item)
        item.moveTo(CappedSortationEstimateStatus.PENDING)
        store.save(item)
        note("$code is pending")
    }

    fun settle(code: String, reason: String) {
        val item = store.find(code) ?: error("capped sortation estimate $code not found")
        if (item.isTerminal()) {
            note("$code already closed")
            return
        }
        item.moveTo(CappedSortationEstimateStatus.CLOSED)
        store.save(item)
        note("$code is closed: $reason")
    }

    fun audit(cutoff: Int): Int {
        var changed = 0
        for (item in store.list()) {
            if (item.isTerminal()) {
                continue
            }
            if (item.endsAt > cutoff) {
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

fun newCappedSortationEstimateManager(store: CappedSortationEstimateStore, clock: Clock): CappedSortationEstimateManager {
    return CappedSortationEstimateManager(
        store = store,
        validator = defaultCappedSortationEstimateValidator(),
        clock = clock,
        journal = mutableListOf<String>(),
    )
}
