package com.acme.transport.lastmile.locker.lockertarget

import com.acme.transport.lastmile.clock.Clock

/** Moves scheduled locker reviews through their life cycle and keeps a journal of what it did. */
data class ScheduledLockerReviewManager(
    var store: ScheduledLockerReviewStore,
    var validator: ScheduledLockerReviewValidator,
    var clock: Clock,
    var journal: MutableList<String>,
) {
    private fun note(message: String) {
        journal.add("${clock.now()} $message")
    }

    fun start(code: String) {
        val item = store.find(code) ?: error("scheduled locker review $code not found")
        validator.requireValid(item)
        item.moveTo(ScheduledLockerReviewStatus.IN_REVIEW)
        store.save(item)
        note("$code is in review")
    }

    fun close(code: String, reason: String) {
        val item = store.find(code) ?: error("scheduled locker review $code not found")
        if (item.isTerminal()) {
            note("$code already closed")
            return
        }
        item.moveTo(ScheduledLockerReviewStatus.ARCHIVED)
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

fun newScheduledLockerReviewManager(store: ScheduledLockerReviewStore, clock: Clock): ScheduledLockerReviewManager {
    return ScheduledLockerReviewManager(
        store = store,
        validator = defaultScheduledLockerReviewValidator(),
        clock = clock,
        journal = mutableListOf<String>(),
    )
}
