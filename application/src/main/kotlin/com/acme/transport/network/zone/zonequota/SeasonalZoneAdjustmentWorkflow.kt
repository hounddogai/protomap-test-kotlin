package com.acme.transport.network.zone.zonequota

import com.acme.transport.network.clock.Clock

/** Moves seasonal zone adjustments through their life cycle and keeps a journal of what it did. */
data class SeasonalZoneAdjustmentDesk(
    var store: SeasonalZoneAdjustmentStore,
    var validator: SeasonalZoneAdjustmentValidator,
    var clock: Clock,
    var journal: MutableList<String>,
) {
    private fun note(message: String) {
        journal.add("${clock.now()} $message")
    }

    fun start(code: String) {
        val item = store.find(code) ?: error("seasonal zone adjustment $code not found")
        validator.requireValid(item)
        item.moveTo(SeasonalZoneAdjustmentStatus.IN_REVIEW)
        store.save(item)
        note("$code is in review")
    }

    fun retire(code: String, reason: String) {
        val item = store.find(code) ?: error("seasonal zone adjustment $code not found")
        if (item.isTerminal()) {
            note("$code already closed")
            return
        }
        item.moveTo(SeasonalZoneAdjustmentStatus.ARCHIVED)
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

fun newSeasonalZoneAdjustmentDesk(store: SeasonalZoneAdjustmentStore, clock: Clock): SeasonalZoneAdjustmentDesk {
    return SeasonalZoneAdjustmentDesk(
        store = store,
        validator = defaultSeasonalZoneAdjustmentValidator(),
        clock = clock,
        journal = mutableListOf<String>(),
    )
}
