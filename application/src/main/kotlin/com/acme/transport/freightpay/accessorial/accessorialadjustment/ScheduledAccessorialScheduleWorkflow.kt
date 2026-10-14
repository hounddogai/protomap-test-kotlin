package com.acme.transport.freightpay.accessorial.accessorialadjustment

import com.acme.transport.freightpay.clock.Clock

/**
 * Moves scheduled accessorial schedules through their life cycle and keeps a journal of what it did.
 */
data class ScheduledAccessorialScheduleDesk(
    var store: ScheduledAccessorialScheduleStore,
    var validator: ScheduledAccessorialScheduleValidator,
    var clock: Clock,
    var journal: MutableList<String>,
) {
    private fun note(message: String) {
        journal.add("${clock.now()} $message")
    }

    fun open(code: String) {
        val item = store.find(code) ?: error("scheduled accessorial schedule $code not found")
        validator.requireValid(item)
        item.moveTo(ScheduledAccessorialScheduleStatus.PENDING)
        store.save(item)
        note("$code is pending")
    }

    fun finish(code: String, reason: String) {
        val item = store.find(code) ?: error("scheduled accessorial schedule $code not found")
        if (item.isTerminal()) {
            note("$code already closed")
            return
        }
        item.moveTo(ScheduledAccessorialScheduleStatus.CLOSED)
        store.save(item)
        note("$code is closed: $reason")
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

fun newScheduledAccessorialScheduleDesk(store: ScheduledAccessorialScheduleStore, clock: Clock): ScheduledAccessorialScheduleDesk {
    return ScheduledAccessorialScheduleDesk(
        store = store,
        validator = defaultScheduledAccessorialScheduleValidator(),
        clock = clock,
        journal = mutableListOf<String>(),
    )
}
