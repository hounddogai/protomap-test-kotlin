package com.acme.transport.fleetops.serviceplan.planpreference

import com.acme.transport.fleetops.clock.Clock

/**
 * Moves weighted service plan templates through their life cycle and keeps a journal of what it did.
 */
data class WeightedServicePlanTemplateCoordinator(
    var store: WeightedServicePlanTemplateStore,
    var validator: WeightedServicePlanTemplateValidator,
    var clock: Clock,
    var journal: MutableList<String>,
) {
    private fun note(message: String) {
        journal.add("${clock.now()} $message")
    }

    fun open(code: String) {
        val item = store.find(code) ?: error("weighted service plan template $code not found")
        validator.requireValid(item)
        item.moveTo(WeightedServicePlanTemplateStatus.APPROVED)
        store.save(item)
        note("$code is approved")
    }

    fun finish(code: String, reason: String) {
        val item = store.find(code) ?: error("weighted service plan template $code not found")
        if (item.isTerminal()) {
            note("$code already closed")
            return
        }
        item.moveTo(WeightedServicePlanTemplateStatus.WITHDRAWN)
        store.save(item)
        note("$code is withdrawn: $reason")
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

fun newWeightedServicePlanTemplateCoordinator(store: WeightedServicePlanTemplateStore, clock: Clock): WeightedServicePlanTemplateCoordinator {
    return WeightedServicePlanTemplateCoordinator(
        store = store,
        validator = defaultWeightedServicePlanTemplateValidator(),
        clock = clock,
        journal = mutableListOf<String>(),
    )
}
