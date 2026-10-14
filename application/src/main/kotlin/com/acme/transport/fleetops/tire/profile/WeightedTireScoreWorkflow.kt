package com.acme.transport.fleetops.tire.profile

import com.acme.transport.fleetops.clock.Clock

/** Moves weighted tire scores through their life cycle and keeps a journal of what it did. */
data class WeightedTireScoreCoordinator(
    var store: WeightedTireScoreStore,
    var validator: WeightedTireScoreValidator,
    var clock: Clock,
    var journal: MutableList<String>,
) {
    private fun note(message: String) {
        journal.add("${clock.now()} $message")
    }

    fun advance(code: String) {
        val item = store.find(code) ?: error("weighted tire score $code not found")
        validator.requireValid(item)
        item.moveTo(WeightedTireScoreStatus.PENDING)
        store.save(item)
        note("$code is pending")
    }

    fun finish(code: String, reason: String) {
        val item = store.find(code) ?: error("weighted tire score $code not found")
        if (item.isTerminal()) {
            note("$code already closed")
            return
        }
        item.moveTo(WeightedTireScoreStatus.CLOSED)
        store.save(item)
        note("$code is closed: $reason")
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

fun newWeightedTireScoreCoordinator(store: WeightedTireScoreStore, clock: Clock): WeightedTireScoreCoordinator {
    return WeightedTireScoreCoordinator(
        store = store,
        validator = defaultWeightedTireScoreValidator(),
        clock = clock,
        journal = mutableListOf<String>(),
    )
}
