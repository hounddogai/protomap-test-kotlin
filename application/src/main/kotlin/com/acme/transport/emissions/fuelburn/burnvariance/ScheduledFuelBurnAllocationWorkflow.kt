package com.acme.transport.emissions.fuelburn.burnvariance

import com.acme.transport.emissions.clock.Clock

/**
 * Moves scheduled fuel burn allocations through their life cycle and keeps a journal of what it did.
 */
data class ScheduledFuelBurnAllocationCoordinator(
    var store: ScheduledFuelBurnAllocationStore,
    var validator: ScheduledFuelBurnAllocationValidator,
    var clock: Clock,
    var journal: MutableList<String>,
) {
    private fun note(message: String) {
        journal.add("${clock.now()} $message")
    }

    fun open(code: String) {
        val item = store.find(code) ?: error("scheduled fuel burn allocation $code not found")
        validator.requireValid(item)
        item.moveTo(ScheduledFuelBurnAllocationStatus.VALIDATED)
        store.save(item)
        note("$code is validated")
    }

    fun complete(code: String, reason: String) {
        val item = store.find(code) ?: error("scheduled fuel burn allocation $code not found")
        if (item.isTerminal()) {
            note("$code already closed")
            return
        }
        item.moveTo(ScheduledFuelBurnAllocationStatus.RETIRED)
        store.save(item)
        note("$code is retired: $reason")
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

fun newScheduledFuelBurnAllocationCoordinator(store: ScheduledFuelBurnAllocationStore, clock: Clock): ScheduledFuelBurnAllocationCoordinator {
    return ScheduledFuelBurnAllocationCoordinator(
        store = store,
        validator = defaultScheduledFuelBurnAllocationValidator(),
        clock = clock,
        journal = mutableListOf<String>(),
    )
}
