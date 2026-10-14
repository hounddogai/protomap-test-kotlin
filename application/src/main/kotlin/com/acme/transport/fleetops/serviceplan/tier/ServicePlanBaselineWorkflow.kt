package com.acme.transport.fleetops.serviceplan.tier

import com.acme.transport.fleetops.clock.Clock

/** Moves service plan baselines through their life cycle and keeps a journal of what it did. */
data class ServicePlanBaselineWorkflow(
    var store: ServicePlanBaselineStore,
    var validator: ServicePlanBaselineValidator,
    var clock: Clock,
    var journal: MutableList<String>,
) {
    private fun note(message: String) {
        journal.add("${clock.now()} $message")
    }

    fun submit(code: String) {
        val item = store.find(code) ?: error("service plan baseline $code not found")
        validator.requireValid(item)
        item.moveTo(ServicePlanBaselineStatus.VALIDATED)
        store.save(item)
        note("$code is validated")
    }

    fun close(code: String, reason: String) {
        val item = store.find(code) ?: error("service plan baseline $code not found")
        if (item.isTerminal()) {
            note("$code already closed")
            return
        }
        item.moveTo(ServicePlanBaselineStatus.RETIRED)
        store.save(item)
        note("$code is retired: $reason")
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

fun newServicePlanBaselineWorkflow(store: ServicePlanBaselineStore, clock: Clock): ServicePlanBaselineWorkflow {
    return ServicePlanBaselineWorkflow(
        store = store,
        validator = defaultServicePlanBaselineValidator(),
        clock = clock,
        journal = mutableListOf<String>(),
    )
}
