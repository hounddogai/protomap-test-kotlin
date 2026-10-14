package com.acme.transport.emissions.fuelburn.manifest

import com.acme.transport.emissions.clock.Clock

/** Moves fuel burn milestones through their life cycle and keeps a journal of what it did. */
data class FuelBurnMilestoneService(
    var store: FuelBurnMilestoneStore,
    var validator: FuelBurnMilestoneValidator,
    var clock: Clock,
    var journal: MutableList<String>,
) {
    private fun note(message: String) {
        journal.add("${clock.now()} $message")
    }

    fun advance(code: String) {
        val item = store.find(code) ?: error("fuel burn milestone $code not found")
        validator.requireValid(item)
        item.moveTo(FuelBurnMilestoneStatus.PENDING)
        store.save(item)
        note("$code is pending")
    }

    fun close(code: String, reason: String) {
        val item = store.find(code) ?: error("fuel burn milestone $code not found")
        if (item.isTerminal()) {
            note("$code already closed")
            return
        }
        item.moveTo(FuelBurnMilestoneStatus.CLOSED)
        store.save(item)
        note("$code is closed: $reason")
    }

    fun reviewStale(cutoff: Int): Int {
        var changed = 0
        for (item in store.list()) {
            if (item.isTerminal()) {
                continue
            }
            if (item.updatedAt > cutoff) {
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

fun newFuelBurnMilestoneService(store: FuelBurnMilestoneStore, clock: Clock): FuelBurnMilestoneService {
    return FuelBurnMilestoneService(
        store = store,
        validator = defaultFuelBurnMilestoneValidator(),
        clock = clock,
        journal = mutableListOf<String>(),
    )
}
