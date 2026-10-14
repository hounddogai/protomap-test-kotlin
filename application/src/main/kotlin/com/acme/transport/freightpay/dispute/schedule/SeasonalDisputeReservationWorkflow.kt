package com.acme.transport.freightpay.dispute.schedule

import com.acme.transport.freightpay.clock.Clock

/** Moves seasonal dispute reservations through their life cycle and keeps a journal of what it did. */
data class SeasonalDisputeReservationService(
    var store: SeasonalDisputeReservationStore,
    var validator: SeasonalDisputeReservationValidator,
    var clock: Clock,
    var journal: MutableList<String>,
) {
    private fun note(message: String) {
        journal.add("${clock.now()} $message")
    }

    fun advance(code: String) {
        val item = store.find(code) ?: error("seasonal dispute reservation $code not found")
        validator.requireValid(item)
        item.moveTo(SeasonalDisputeReservationStatus.IN_REVIEW)
        store.save(item)
        note("$code is in review")
    }

    fun retire(code: String, reason: String) {
        val item = store.find(code) ?: error("seasonal dispute reservation $code not found")
        if (item.isTerminal()) {
            note("$code already closed")
            return
        }
        item.moveTo(SeasonalDisputeReservationStatus.ARCHIVED)
        store.save(item)
        note("$code is archived: $reason")
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

fun newSeasonalDisputeReservationService(store: SeasonalDisputeReservationStore, clock: Clock): SeasonalDisputeReservationService {
    return SeasonalDisputeReservationService(
        store = store,
        validator = defaultSeasonalDisputeReservationValidator(),
        clock = clock,
        journal = mutableListOf<String>(),
    )
}
