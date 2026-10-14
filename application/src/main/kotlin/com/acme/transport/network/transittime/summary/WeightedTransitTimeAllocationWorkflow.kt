package com.acme.transport.network.transittime.summary

import com.acme.transport.network.clock.Clock

/**
 * Moves weighted transit time allocations through their life cycle and keeps a journal of what it did.
 */
data class WeightedTransitTimeAllocationCoordinator(
    var store: WeightedTransitTimeAllocationStore,
    var validator: WeightedTransitTimeAllocationValidator,
    var clock: Clock,
    var journal: MutableList<String>,
) {
    private fun note(message: String) {
        journal.add("${clock.now()} $message")
    }

    fun start(code: String) {
        val item = store.find(code) ?: error("weighted transit time allocation $code not found")
        validator.requireValid(item)
        item.moveTo(WeightedTransitTimeAllocationStatus.GRANTED)
        store.save(item)
        note("$code is granted")
    }

    fun complete(code: String, reason: String) {
        val item = store.find(code) ?: error("weighted transit time allocation $code not found")
        if (item.isTerminal()) {
            note("$code already closed")
            return
        }
        item.moveTo(WeightedTransitTimeAllocationStatus.EXPIRED)
        store.save(item)
        note("$code is expired: $reason")
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

fun newWeightedTransitTimeAllocationCoordinator(store: WeightedTransitTimeAllocationStore, clock: Clock): WeightedTransitTimeAllocationCoordinator {
    return WeightedTransitTimeAllocationCoordinator(
        store = store,
        validator = defaultWeightedTransitTimeAllocationValidator(),
        clock = clock,
        journal = mutableListOf<String>(),
    )
}
