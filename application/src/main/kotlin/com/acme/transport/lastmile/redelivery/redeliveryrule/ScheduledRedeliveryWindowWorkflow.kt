package com.acme.transport.lastmile.redelivery.redeliveryrule

import com.acme.transport.lastmile.clock.Clock

/** Moves scheduled redelivery windows through their life cycle and keeps a journal of what it did. */
data class ScheduledRedeliveryWindowDesk(
    var store: ScheduledRedeliveryWindowStore,
    var validator: ScheduledRedeliveryWindowValidator,
    var clock: Clock,
    var journal: MutableList<String>,
) {
    private fun note(message: String) {
        journal.add("${clock.now()} $message")
    }

    fun submit(code: String) {
        val item = store.find(code) ?: error("scheduled redelivery window $code not found")
        validator.requireValid(item)
        item.moveTo(ScheduledRedeliveryWindowStatus.PENDING)
        store.save(item)
        note("$code is pending")
    }

    fun settle(code: String, reason: String) {
        val item = store.find(code) ?: error("scheduled redelivery window $code not found")
        if (item.isTerminal()) {
            note("$code already closed")
            return
        }
        item.moveTo(ScheduledRedeliveryWindowStatus.CLOSED)
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

fun newScheduledRedeliveryWindowDesk(store: ScheduledRedeliveryWindowStore, clock: Clock): ScheduledRedeliveryWindowDesk {
    return ScheduledRedeliveryWindowDesk(
        store = store,
        validator = defaultScheduledRedeliveryWindowValidator(),
        clock = clock,
        journal = mutableListOf<String>(),
    )
}
