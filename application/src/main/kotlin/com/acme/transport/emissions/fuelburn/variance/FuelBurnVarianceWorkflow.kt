package com.acme.transport.emissions.fuelburn.variance

import com.acme.transport.emissions.clock.Clock

/** Moves fuel burn variances through their life cycle and keeps a journal of what it did. */
data class FuelBurnVarianceDesk(
    var store: FuelBurnVarianceStore,
    var validator: FuelBurnVarianceValidator,
    var clock: Clock,
    var journal: MutableList<String>,
) {
    private fun note(message: String) {
        journal.add("${clock.now()} $message")
    }

    fun open(code: String) {
        val item = store.find(code) ?: error("fuel burn variance $code not found")
        validator.requireValid(item)
        item.moveTo(FuelBurnVarianceStatus.WARMING)
        store.save(item)
        note("$code is warming")
    }

    fun retire(code: String, reason: String) {
        val item = store.find(code) ?: error("fuel burn variance $code not found")
        if (item.isTerminal()) {
            note("$code already closed")
            return
        }
        item.moveTo(FuelBurnVarianceStatus.STOPPED)
        store.save(item)
        note("$code is stopped: $reason")
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

fun newFuelBurnVarianceDesk(store: FuelBurnVarianceStore, clock: Clock): FuelBurnVarianceDesk {
    return FuelBurnVarianceDesk(
        store = store,
        validator = defaultFuelBurnVarianceValidator(),
        clock = clock,
        journal = mutableListOf<String>(),
    )
}
