package com.acme.transport.fleetops.serviceplan.score

import com.acme.transport.fleetops.clock.Clock

/** Moves service plan scores through their life cycle and keeps a journal of what it did. */
data class ServicePlanScoreCoordinator(
    var store: ServicePlanScoreStore,
    var validator: ServicePlanScoreValidator,
    var clock: Clock,
    var journal: MutableList<String>,
) {
    private fun note(message: String) {
        journal.add("${clock.now()} $message")
    }

    fun submit(code: String) {
        val item = store.find(code) ?: error("service plan score $code not found")
        validator.requireValid(item)
        item.moveTo(ServicePlanScoreStatus.WARMING)
        store.save(item)
        note("$code is warming")
    }

    fun settle(code: String, reason: String) {
        val item = store.find(code) ?: error("service plan score $code not found")
        if (item.isTerminal()) {
            note("$code already closed")
            return
        }
        item.moveTo(ServicePlanScoreStatus.STOPPED)
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

fun newServicePlanScoreCoordinator(store: ServicePlanScoreStore, clock: Clock): ServicePlanScoreCoordinator {
    return ServicePlanScoreCoordinator(
        store = store,
        validator = defaultServicePlanScoreValidator(),
        clock = clock,
        journal = mutableListOf<String>(),
    )
}
