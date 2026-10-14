package com.acme.transport.fleetops.serviceplan.legacyplanquota

import com.acme.transport.fleetops.clock.Clock
import com.acme.transport.kit.flags.Registry

/**
 * Moves weighted service plan milestones through their life cycle and keeps a journal of what it did.
 */
data class WeightedServicePlanMilestoneManager(
    var store: WeightedServicePlanMilestoneStore,
    var validator: WeightedServicePlanMilestoneValidator,
    var clock: Clock,
    var journal: MutableList<String>,
    var engine: WeightedServicePlanMilestoneEngine,
) {
    private fun note(message: String) {
        journal.add("${clock.now()} $message")
    }

    fun start(code: String) {
        val item = store.find(code) ?: error("weighted service plan milestone $code not found")
        validator.requireValid(item)
        item.moveTo(WeightedServicePlanMilestoneStatus.GRANTED)
        store.save(item)
        note("$code is granted")
    }

    fun complete(code: String, reason: String) {
        val item = store.find(code) ?: error("weighted service plan milestone $code not found")
        if (item.isTerminal()) {
            note("$code already closed")
            return
        }
        item.moveTo(WeightedServicePlanMilestoneStatus.EXPIRED)
        store.save(item)
        note("$code is expired: $reason")
    }

    fun evaluate(code: String): Double {
        val item = store.find(code) ?: error("weighted service plan milestone $code not found")
        val result = engine.evaluate(item)
        note("$code evaluated at $result")
        return result
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

fun newWeightedServicePlanMilestoneManager(store: WeightedServicePlanMilestoneStore, clock: Clock, flags: Registry): WeightedServicePlanMilestoneManager {
    return WeightedServicePlanMilestoneManager(
        store = store,
        validator = defaultWeightedServicePlanMilestoneValidator(),
        clock = clock,
        journal = mutableListOf<String>(),
        engine = defaultWeightedServicePlanMilestoneEngine(flags),
    )
}
