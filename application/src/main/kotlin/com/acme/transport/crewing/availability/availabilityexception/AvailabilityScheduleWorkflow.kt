package com.acme.transport.crewing.availability.availabilityexception

import com.acme.transport.crewing.clock.Clock

/** Moves availability schedules through their life cycle and keeps a journal of what it did. */
data class AvailabilityScheduleDesk(
    var store: AvailabilityScheduleStore,
    var validator: AvailabilityScheduleValidator,
    var clock: Clock,
    var journal: MutableList<String>,
) {
    private fun note(message: String) {
        journal.add("${clock.now()} $message")
    }

    fun start(code: String) {
        val item = store.find(code) ?: error("availability schedule $code not found")
        validator.requireValid(item)
        item.moveTo(AvailabilityScheduleStatus.WARMING)
        store.save(item)
        note("$code is warming")
    }

    fun settle(code: String, reason: String) {
        val item = store.find(code) ?: error("availability schedule $code not found")
        if (item.isTerminal()) {
            note("$code already closed")
            return
        }
        item.moveTo(AvailabilityScheduleStatus.STOPPED)
        store.save(item)
        note("$code is stopped: $reason")
    }

    fun scan(cutoff: Int): Int {
        var changed = 0
        for (item in store.list()) {
            if (item.isTerminal()) {
                continue
            }
            if (item.expiresAt > cutoff) {
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

fun newAvailabilityScheduleDesk(store: AvailabilityScheduleStore, clock: Clock): AvailabilityScheduleDesk {
    return AvailabilityScheduleDesk(
        store = store,
        validator = defaultAvailabilityScheduleValidator(),
        clock = clock,
        journal = mutableListOf<String>(),
    )
}
