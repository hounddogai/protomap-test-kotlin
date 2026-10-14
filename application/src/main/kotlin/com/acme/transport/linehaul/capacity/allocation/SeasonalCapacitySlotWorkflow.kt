package com.acme.transport.linehaul.capacity.allocation

import com.acme.transport.linehaul.clock.Clock

/** Moves seasonal capacity slots through their life cycle and keeps a journal of what it did. */
data class SeasonalCapacitySlotCoordinator(
    var store: SeasonalCapacitySlotStore,
    var validator: SeasonalCapacitySlotValidator,
    var clock: Clock,
    var journal: MutableList<String>,
) {
    private fun note(message: String) {
        journal.add("${clock.now()} $message")
    }

    fun open(code: String) {
        val item = store.find(code) ?: error("seasonal capacity slot $code not found")
        validator.requireValid(item)
        item.moveTo(SeasonalCapacitySlotStatus.GRANTED)
        store.save(item)
        note("$code is granted")
    }

    fun close(code: String, reason: String) {
        val item = store.find(code) ?: error("seasonal capacity slot $code not found")
        if (item.isTerminal()) {
            note("$code already closed")
            return
        }
        item.moveTo(SeasonalCapacitySlotStatus.EXPIRED)
        store.save(item)
        note("$code is expired: $reason")
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

fun newSeasonalCapacitySlotCoordinator(store: SeasonalCapacitySlotStore, clock: Clock): SeasonalCapacitySlotCoordinator {
    return SeasonalCapacitySlotCoordinator(
        store = store,
        validator = defaultSeasonalCapacitySlotValidator(),
        clock = clock,
        journal = mutableListOf<String>(),
    )
}
