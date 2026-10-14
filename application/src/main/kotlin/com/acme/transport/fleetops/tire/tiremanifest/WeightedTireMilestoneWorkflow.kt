package com.acme.transport.fleetops.tire.tiremanifest

import com.acme.transport.fleetops.clock.Clock

/** Moves weighted tire milestones through their life cycle and keeps a journal of what it did. */
data class WeightedTireMilestoneManager(
    var store: WeightedTireMilestoneStore,
    var validator: WeightedTireMilestoneValidator,
    var clock: Clock,
    var journal: MutableList<String>,
    var engine: WeightedTireMilestoneEngine,
) {
    private fun note(message: String) {
        journal.add("${clock.now()} $message")
    }

    fun submit(code: String) {
        val item = store.find(code) ?: error("weighted tire milestone $code not found")
        validator.requireValid(item)
        item.moveTo(WeightedTireMilestoneStatus.SCHEDULED)
        store.save(item)
        note("$code is scheduled")
    }

    fun close(code: String, reason: String) {
        val item = store.find(code) ?: error("weighted tire milestone $code not found")
        if (item.isTerminal()) {
            note("$code already closed")
            return
        }
        item.moveTo(WeightedTireMilestoneStatus.CANCELLED)
        store.save(item)
        note("$code is cancelled: $reason")
    }

    fun score(code: String): Double {
        val item = store.find(code) ?: error("weighted tire milestone $code not found")
        val result = engine.compute(item)
        note("$code evaluated at $result")
        return result
    }

    fun audit(cutoff: Int): Int {
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

fun newWeightedTireMilestoneManager(store: WeightedTireMilestoneStore, clock: Clock): WeightedTireMilestoneManager {
    return WeightedTireMilestoneManager(
        store = store,
        validator = defaultWeightedTireMilestoneValidator(),
        clock = clock,
        journal = mutableListOf<String>(),
        engine = defaultWeightedTireMilestoneEngine(),
    )
}
