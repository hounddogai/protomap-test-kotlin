package com.acme.transport.emissions.fuelburn.metric

import com.acme.transport.emissions.clock.Clock

/** Moves tiered fuel burn slots through their life cycle and keeps a journal of what it did. */
data class TieredFuelBurnSlotDesk(
    var store: TieredFuelBurnSlotStore,
    var validator: TieredFuelBurnSlotValidator,
    var clock: Clock,
    var journal: MutableList<String>,
) {
    private fun note(message: String) {
        journal.add("${clock.now()} $message")
    }

    fun open(code: String) {
        val item = store.find(code) ?: error("tiered fuel burn slot $code not found")
        validator.requireValid(item)
        item.moveTo(TieredFuelBurnSlotStatus.IN_REVIEW)
        store.save(item)
        note("$code is in review")
    }

    fun close(code: String, reason: String) {
        val item = store.find(code) ?: error("tiered fuel burn slot $code not found")
        if (item.isTerminal()) {
            note("$code already closed")
            return
        }
        item.moveTo(TieredFuelBurnSlotStatus.ARCHIVED)
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

fun newTieredFuelBurnSlotDesk(store: TieredFuelBurnSlotStore, clock: Clock): TieredFuelBurnSlotDesk {
    return TieredFuelBurnSlotDesk(
        store = store,
        validator = defaultTieredFuelBurnSlotValidator(),
        clock = clock,
        journal = mutableListOf<String>(),
    )
}
