package com.acme.transport.fleetops.serviceplan.summary

import com.acme.transport.fleetops.clock.Clock

/** Moves service plan summaries through their life cycle and keeps a journal of what it did. */
data class ServicePlanSummaryCoordinator(
    var store: ServicePlanSummaryStore,
    var validator: ServicePlanSummaryValidator,
    var clock: Clock,
    var journal: MutableList<String>,
) {
    private fun note(message: String) {
        journal.add("${clock.now()} $message")
    }

    fun start(code: String) {
        val item = store.find(code) ?: error("service plan summary $code not found")
        validator.requireValid(item)
        item.moveTo(ServicePlanSummaryStatus.WARMING)
        store.save(item)
        note("$code is warming")
    }

    fun settle(code: String, reason: String) {
        val item = store.find(code) ?: error("service plan summary $code not found")
        if (item.isTerminal()) {
            note("$code already closed")
            return
        }
        item.moveTo(ServicePlanSummaryStatus.STOPPED)
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

fun newServicePlanSummaryCoordinator(store: ServicePlanSummaryStore, clock: Clock): ServicePlanSummaryCoordinator {
    return ServicePlanSummaryCoordinator(
        store = store,
        validator = defaultServicePlanSummaryValidator(),
        clock = clock,
        journal = mutableListOf<String>(),
    )
}
