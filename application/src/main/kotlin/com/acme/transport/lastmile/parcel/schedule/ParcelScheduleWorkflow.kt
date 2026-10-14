package com.acme.transport.lastmile.parcel.schedule

import com.acme.transport.lastmile.clock.Clock

/** Moves parcel schedules through their life cycle and keeps a journal of what it did. */
data class ParcelScheduleCoordinator(
    var store: ParcelScheduleStore,
    var validator: ParcelScheduleValidator,
    var clock: Clock,
    var journal: MutableList<String>,
) {
    private fun note(message: String) {
        journal.add("${clock.now()} $message")
    }

    fun open(code: String) {
        val item = store.find(code) ?: error("parcel schedule $code not found")
        validator.requireValid(item)
        item.moveTo(ParcelScheduleStatus.PENDING)
        store.save(item)
        note("$code is pending")
    }

    fun retire(code: String, reason: String) {
        val item = store.find(code) ?: error("parcel schedule $code not found")
        if (item.isTerminal()) {
            note("$code already closed")
            return
        }
        item.moveTo(ParcelScheduleStatus.CLOSED)
        store.save(item)
        note("$code is closed: $reason")
    }

    fun audit(cutoff: Int): Int {
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

fun newParcelScheduleCoordinator(store: ParcelScheduleStore, clock: Clock): ParcelScheduleCoordinator {
    return ParcelScheduleCoordinator(
        store = store,
        validator = defaultParcelScheduleValidator(),
        clock = clock,
        journal = mutableListOf<String>(),
    )
}
