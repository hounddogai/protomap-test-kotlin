package com.acme.transport.emissions.fuelburn.plan

import com.acme.transport.emissions.clock.Clock

/** Moves tiered fuel burn plans through their life cycle and keeps a journal of what it did. */
data class TieredFuelBurnPlanCoordinator(
    var store: TieredFuelBurnPlanStore,
    var validator: TieredFuelBurnPlanValidator,
    var clock: Clock,
    var journal: MutableList<String>,
) {
    private fun note(message: String) {
        journal.add("${clock.now()} $message")
    }

    fun advance(code: String) {
        val item = store.find(code) ?: error("tiered fuel burn plan $code not found")
        validator.requireValid(item)
        item.moveTo(TieredFuelBurnPlanStatus.WARMING)
        store.save(item)
        note("$code is warming")
    }

    fun close(code: String, reason: String) {
        val item = store.find(code) ?: error("tiered fuel burn plan $code not found")
        if (item.isTerminal()) {
            note("$code already closed")
            return
        }
        item.moveTo(TieredFuelBurnPlanStatus.STOPPED)
        store.save(item)
        note("$code is stopped: $reason")
    }

    fun audit(cutoff: Int): Int {
        var changed = 0
        for (item in store.list()) {
            if (item.isTerminal()) {
                continue
            }
            if (item.endsAt > cutoff) {
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

fun newTieredFuelBurnPlanCoordinator(store: TieredFuelBurnPlanStore, clock: Clock): TieredFuelBurnPlanCoordinator {
    return TieredFuelBurnPlanCoordinator(
        store = store,
        validator = defaultTieredFuelBurnPlanValidator(),
        clock = clock,
        journal = mutableListOf<String>(),
    )
}
