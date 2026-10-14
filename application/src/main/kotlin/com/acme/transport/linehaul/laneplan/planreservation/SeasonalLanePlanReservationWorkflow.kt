package com.acme.transport.linehaul.laneplan.planreservation

import com.acme.transport.linehaul.clock.Clock

/**
 * Moves seasonal lane plan reservations through their life cycle and keeps a journal of what it did.
 */
data class SeasonalLanePlanReservationService(
    var store: SeasonalLanePlanReservationStore,
    var validator: SeasonalLanePlanReservationValidator,
    var clock: Clock,
    var journal: MutableList<String>,
) {
    private fun note(message: String) {
        journal.add("${clock.now()} $message")
    }

    fun open(code: String) {
        val item = store.find(code) ?: error("seasonal lane plan reservation $code not found")
        validator.requireValid(item)
        item.moveTo(SeasonalLanePlanReservationStatus.IN_REVIEW)
        store.save(item)
        note("$code is in review")
    }

    fun complete(code: String, reason: String) {
        val item = store.find(code) ?: error("seasonal lane plan reservation $code not found")
        if (item.isTerminal()) {
            note("$code already closed")
            return
        }
        item.moveTo(SeasonalLanePlanReservationStatus.ARCHIVED)
        store.save(item)
        note("$code is archived: $reason")
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

fun newSeasonalLanePlanReservationService(store: SeasonalLanePlanReservationStore, clock: Clock): SeasonalLanePlanReservationService {
    return SeasonalLanePlanReservationService(
        store = store,
        validator = defaultSeasonalLanePlanReservationValidator(),
        clock = clock,
        journal = mutableListOf<String>(),
    )
}
