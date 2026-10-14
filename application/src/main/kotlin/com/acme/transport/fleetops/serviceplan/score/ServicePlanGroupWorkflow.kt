package com.acme.transport.fleetops.serviceplan.score

import com.acme.transport.fleetops.clock.Clock

/** Moves service plan groups through their life cycle and keeps a journal of what it did. */
data class ServicePlanGroupManager(
    var store: ServicePlanGroupStore,
    var validator: ServicePlanGroupValidator,
    var clock: Clock,
    var journal: MutableList<String>,
) {
    private fun note(message: String) {
        journal.add("${clock.now()} $message")
    }

    fun submit(code: String) {
        val item = store.find(code) ?: error("service plan group $code not found")
        validator.requireValid(item)
        item.moveTo(ServicePlanGroupStatus.SCHEDULED)
        store.save(item)
        note("$code is scheduled")
    }

    fun complete(code: String, reason: String) {
        val item = store.find(code) ?: error("service plan group $code not found")
        if (item.isTerminal()) {
            note("$code already closed")
            return
        }
        item.moveTo(ServicePlanGroupStatus.CANCELLED)
        store.save(item)
        note("$code is cancelled: $reason")
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

fun newServicePlanGroupManager(store: ServicePlanGroupStore, clock: Clock): ServicePlanGroupManager {
    return ServicePlanGroupManager(
        store = store,
        validator = defaultServicePlanGroupValidator(),
        clock = clock,
        journal = mutableListOf<String>(),
    )
}
