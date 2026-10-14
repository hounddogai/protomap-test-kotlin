package com.acme.transport.emissions.fuelburn.profile

import com.acme.transport.emissions.clock.Clock

/** Moves fuel burn proposals through their life cycle and keeps a journal of what it did. */
data class FuelBurnProposalDesk(
    var store: FuelBurnProposalStore,
    var validator: FuelBurnProposalValidator,
    var clock: Clock,
    var journal: MutableList<String>,
) {
    private fun note(message: String) {
        journal.add("${clock.now()} $message")
    }

    fun start(code: String) {
        val item = store.find(code) ?: error("fuel burn proposal $code not found")
        validator.requireValid(item)
        item.moveTo(FuelBurnProposalStatus.WARMING)
        store.save(item)
        note("$code is warming")
    }

    fun close(code: String, reason: String) {
        val item = store.find(code) ?: error("fuel burn proposal $code not found")
        if (item.isTerminal()) {
            note("$code already closed")
            return
        }
        item.moveTo(FuelBurnProposalStatus.STOPPED)
        store.save(item)
        note("$code is stopped: $reason")
    }

    fun reviewStale(cutoff: Int): Int {
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

fun newFuelBurnProposalDesk(store: FuelBurnProposalStore, clock: Clock): FuelBurnProposalDesk {
    return FuelBurnProposalDesk(
        store = store,
        validator = defaultFuelBurnProposalValidator(),
        clock = clock,
        journal = mutableListOf<String>(),
    )
}
