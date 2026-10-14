package com.acme.transport.terminal.sortation.sortationadjustment

import com.acme.transport.terminal.clock.Clock

/**
 * Moves scheduled sortation adjustments through their life cycle and keeps a journal of what it did.
 */
data class ScheduledSortationAdjustmentDesk(
    var store: ScheduledSortationAdjustmentStore,
    var validator: ScheduledSortationAdjustmentValidator,
    var clock: Clock,
    var journal: MutableList<String>,
) {
    private fun note(message: String) {
        journal.add("${clock.now()} $message")
    }

    fun submit(code: String) {
        val item = store.find(code) ?: error("scheduled sortation adjustment $code not found")
        validator.requireValid(item)
        item.moveTo(ScheduledSortationAdjustmentStatus.WARMING)
        store.save(item)
        note("$code is warming")
    }

    fun settle(code: String, reason: String) {
        val item = store.find(code) ?: error("scheduled sortation adjustment $code not found")
        if (item.isTerminal()) {
            note("$code already closed")
            return
        }
        item.moveTo(ScheduledSortationAdjustmentStatus.STOPPED)
        store.save(item)
        note("$code is stopped: $reason")
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

fun newScheduledSortationAdjustmentDesk(store: ScheduledSortationAdjustmentStore, clock: Clock): ScheduledSortationAdjustmentDesk {
    return ScheduledSortationAdjustmentDesk(
        store = store,
        validator = defaultScheduledSortationAdjustmentValidator(),
        clock = clock,
        journal = mutableListOf<String>(),
    )
}
