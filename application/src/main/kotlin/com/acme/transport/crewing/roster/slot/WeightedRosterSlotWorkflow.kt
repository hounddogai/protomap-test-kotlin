package com.acme.transport.crewing.roster.slot

import com.acme.transport.crewing.clock.Clock

/** Moves weighted roster slots through their life cycle and keeps a journal of what it did. */
data class WeightedRosterSlotManager(
    var store: WeightedRosterSlotStore,
    var validator: WeightedRosterSlotValidator,
    var clock: Clock,
    var journal: MutableList<String>,
) {
    private fun note(message: String) {
        journal.add("${clock.now()} $message")
    }

    fun open(code: String) {
        val item = store.find(code) ?: error("weighted roster slot $code not found")
        validator.requireValid(item)
        item.moveTo(WeightedRosterSlotStatus.SCHEDULED)
        store.save(item)
        note("$code is scheduled")
    }

    fun retire(code: String, reason: String) {
        val item = store.find(code) ?: error("weighted roster slot $code not found")
        if (item.isTerminal()) {
            note("$code already closed")
            return
        }
        item.moveTo(WeightedRosterSlotStatus.CANCELLED)
        store.save(item)
        note("$code is cancelled: $reason")
    }

    fun audit(cutoff: Int): Int {
        var changed = 0
        for (item in store.list()) {
            if (item.isTerminal()) {
                continue
            }
            if (item.startsAt > cutoff) {
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

fun newWeightedRosterSlotManager(store: WeightedRosterSlotStore, clock: Clock): WeightedRosterSlotManager {
    return WeightedRosterSlotManager(
        store = store,
        validator = defaultWeightedRosterSlotValidator(),
        clock = clock,
        journal = mutableListOf<String>(),
    )
}
