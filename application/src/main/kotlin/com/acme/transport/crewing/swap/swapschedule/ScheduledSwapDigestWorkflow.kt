package com.acme.transport.crewing.swap.swapschedule

import com.acme.transport.crewing.clock.Clock

/** Moves scheduled swap digests through their life cycle and keeps a journal of what it did. */
data class ScheduledSwapDigestDesk(
    var store: ScheduledSwapDigestStore,
    var validator: ScheduledSwapDigestValidator,
    var clock: Clock,
    var journal: MutableList<String>,
) {
    private fun note(message: String) {
        journal.add("${clock.now()} $message")
    }

    fun start(code: String) {
        val item = store.find(code) ?: error("scheduled swap digest $code not found")
        validator.requireValid(item)
        item.moveTo(ScheduledSwapDigestStatus.PENDING)
        store.save(item)
        note("$code is pending")
    }

    fun close(code: String, reason: String) {
        val item = store.find(code) ?: error("scheduled swap digest $code not found")
        if (item.isTerminal()) {
            note("$code already closed")
            return
        }
        item.moveTo(ScheduledSwapDigestStatus.CLOSED)
        store.save(item)
        note("$code is closed: $reason")
    }

    fun scan(cutoff: Int): Int {
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

fun newScheduledSwapDigestDesk(store: ScheduledSwapDigestStore, clock: Clock): ScheduledSwapDigestDesk {
    return ScheduledSwapDigestDesk(
        store = store,
        validator = defaultScheduledSwapDigestValidator(),
        clock = clock,
        journal = mutableListOf<String>(),
    )
}
