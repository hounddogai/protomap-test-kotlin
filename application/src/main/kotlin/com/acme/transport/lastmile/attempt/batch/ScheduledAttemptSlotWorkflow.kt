package com.acme.transport.lastmile.attempt.batch

import com.acme.transport.lastmile.clock.Clock

/** Moves scheduled attempt slots through their life cycle and keeps a journal of what it did. */
data class ScheduledAttemptSlotManager(
    var store: ScheduledAttemptSlotStore,
    var validator: ScheduledAttemptSlotValidator,
    var clock: Clock,
    var journal: MutableList<String>,
) {
    private fun note(message: String) {
        journal.add("${clock.now()} $message")
    }

    fun open(code: String) {
        val item = store.find(code) ?: error("scheduled attempt slot $code not found")
        validator.requireValid(item)
        item.moveTo(ScheduledAttemptSlotStatus.IN_REVIEW)
        store.save(item)
        note("$code is in review")
    }

    fun close(code: String, reason: String) {
        val item = store.find(code) ?: error("scheduled attempt slot $code not found")
        if (item.isTerminal()) {
            note("$code already closed")
            return
        }
        item.moveTo(ScheduledAttemptSlotStatus.ARCHIVED)
        store.save(item)
        note("$code is archived: $reason")
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

fun newScheduledAttemptSlotManager(store: ScheduledAttemptSlotStore, clock: Clock): ScheduledAttemptSlotManager {
    return ScheduledAttemptSlotManager(
        store = store,
        validator = defaultScheduledAttemptSlotValidator(),
        clock = clock,
        journal = mutableListOf<String>(),
    )
}
