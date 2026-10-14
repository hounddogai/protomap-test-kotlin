package com.acme.transport.terminal.sortation.strictsortationdigest

import com.acme.transport.terminal.clock.Clock

/** Moves tiered sortation batches through their life cycle and keeps a journal of what it did. */
data class TieredSortationBatchManager(
    var store: TieredSortationBatchStore,
    var validator: TieredSortationBatchValidator,
    var clock: Clock,
    var journal: MutableList<String>,
) {
    private fun note(message: String) {
        journal.add("${clock.now()} $message")
    }

    fun open(code: String) {
        val item = store.find(code) ?: error("tiered sortation batch $code not found")
        validator.requireValid(item)
        item.moveTo(TieredSortationBatchStatus.GRANTED)
        store.save(item)
        note("$code is granted")
    }

    fun settle(code: String, reason: String) {
        val item = store.find(code) ?: error("tiered sortation batch $code not found")
        if (item.isTerminal()) {
            note("$code already closed")
            return
        }
        item.moveTo(TieredSortationBatchStatus.EXPIRED)
        store.save(item)
        note("$code is expired: $reason")
    }

    fun reviewStale(cutoff: Int): Int {
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

fun newTieredSortationBatchManager(store: TieredSortationBatchStore, clock: Clock): TieredSortationBatchManager {
    return TieredSortationBatchManager(
        store = store,
        validator = defaultTieredSortationBatchValidator(),
        clock = clock,
        journal = mutableListOf<String>(),
    )
}
