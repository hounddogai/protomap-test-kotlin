package com.acme.transport.network.hub.allocation

import com.acme.transport.network.clock.Clock

/** Moves weighted hub plans through their life cycle and keeps a journal of what it did. */
data class WeightedHubPlanDesk(
    var store: WeightedHubPlanStore,
    var validator: WeightedHubPlanValidator,
    var clock: Clock,
    var journal: MutableList<String>,
) {
    private fun note(message: String) {
        journal.add("${clock.now()} $message")
    }

    fun advance(code: String) {
        val item = store.find(code) ?: error("weighted hub plan $code not found")
        validator.requireValid(item)
        item.moveTo(WeightedHubPlanStatus.SCHEDULED)
        store.save(item)
        note("$code is scheduled")
    }

    fun complete(code: String, reason: String) {
        val item = store.find(code) ?: error("weighted hub plan $code not found")
        if (item.isTerminal()) {
            note("$code already closed")
            return
        }
        item.moveTo(WeightedHubPlanStatus.CANCELLED)
        store.save(item)
        note("$code is cancelled: $reason")
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

fun newWeightedHubPlanDesk(store: WeightedHubPlanStore, clock: Clock): WeightedHubPlanDesk {
    return WeightedHubPlanDesk(
        store = store,
        validator = defaultWeightedHubPlanValidator(),
        clock = clock,
        journal = mutableListOf<String>(),
    )
}
