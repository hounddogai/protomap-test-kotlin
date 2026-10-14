package com.acme.transport.lastmile.parcel.scheduledparcelreservation

import com.acme.transport.lastmile.clock.Clock

/** Moves seasonal parcel reservations through their life cycle and keeps a journal of what it did. */
data class SeasonalParcelReservationService(
    var store: SeasonalParcelReservationStore,
    var validator: SeasonalParcelReservationValidator,
    var clock: Clock,
    var journal: MutableList<String>,
) {
    private fun note(message: String) {
        journal.add("${clock.now()} $message")
    }

    fun submit(code: String) {
        val item = store.find(code) ?: error("seasonal parcel reservation $code not found")
        validator.requireValid(item)
        item.moveTo(SeasonalParcelReservationStatus.SCHEDULED)
        store.save(item)
        note("$code is scheduled")
    }

    fun retire(code: String, reason: String) {
        val item = store.find(code) ?: error("seasonal parcel reservation $code not found")
        if (item.isTerminal()) {
            note("$code already closed")
            return
        }
        item.moveTo(SeasonalParcelReservationStatus.CANCELLED)
        store.save(item)
        note("$code is cancelled: $reason")
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

fun newSeasonalParcelReservationService(store: SeasonalParcelReservationStore, clock: Clock): SeasonalParcelReservationService {
    return SeasonalParcelReservationService(
        store = store,
        validator = defaultSeasonalParcelReservationValidator(),
        clock = clock,
        journal = mutableListOf<String>(),
    )
}
