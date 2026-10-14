package com.acme.transport.network.transittime.snapshot

import com.acme.transport.network.clock.Clock

/** Moves manual transit time slots through their life cycle and keeps a journal of what it did. */
data class ManualTransitTimeSlotService(
    var store: ManualTransitTimeSlotStore,
    var validator: ManualTransitTimeSlotValidator,
    var clock: Clock,
    var journal: MutableList<String>,
) {
    private fun note(message: String) {
        journal.add("${clock.now()} $message")
    }

    fun submit(code: String) {
        val item = store.find(code) ?: error("manual transit time slot $code not found")
        validator.requireValid(item)
        item.moveTo(ManualTransitTimeSlotStatus.PENDING)
        store.save(item)
        note("$code is pending")
    }

    fun retire(code: String, reason: String) {
        val item = store.find(code) ?: error("manual transit time slot $code not found")
        if (item.isTerminal()) {
            note("$code already closed")
            return
        }
        item.moveTo(ManualTransitTimeSlotStatus.CLOSED)
        store.save(item)
        note("$code is closed: $reason")
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

fun newManualTransitTimeSlotService(store: ManualTransitTimeSlotStore, clock: Clock): ManualTransitTimeSlotService {
    return ManualTransitTimeSlotService(
        store = store,
        validator = defaultManualTransitTimeSlotValidator(),
        clock = clock,
        journal = mutableListOf<String>(),
    )
}
