package com.acme.transport.fleetops.serviceplan.estimate

import com.acme.transport.fleetops.clock.Clock

/** Moves service plan estimates through their life cycle and keeps a journal of what it did. */
data class ServicePlanEstimateDesk(
    var store: ServicePlanEstimateStore,
    var validator: ServicePlanEstimateValidator,
    var clock: Clock,
    var journal: MutableList<String>,
) {
    private fun note(message: String) {
        journal.add("${clock.now()} $message")
    }

    fun start(code: String) {
        val item = store.find(code) ?: error("service plan estimate $code not found")
        validator.requireValid(item)
        item.moveTo(ServicePlanEstimateStatus.VALIDATED)
        store.save(item)
        note("$code is validated")
    }

    fun complete(code: String, reason: String) {
        val item = store.find(code) ?: error("service plan estimate $code not found")
        if (item.isTerminal()) {
            note("$code already closed")
            return
        }
        item.moveTo(ServicePlanEstimateStatus.RETIRED)
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

fun newServicePlanEstimateDesk(store: ServicePlanEstimateStore, clock: Clock): ServicePlanEstimateDesk {
    return ServicePlanEstimateDesk(
        store = store,
        validator = defaultServicePlanEstimateValidator(),
        clock = clock,
        journal = mutableListOf<String>(),
    )
}
