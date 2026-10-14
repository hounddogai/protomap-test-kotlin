package com.acme.transport.fleetops.serviceplan.legacyplanquota

import com.acme.transport.fleetops.clock.Clock

/**
 * Moves scheduled service plan metrics through their life cycle and keeps a journal of what it did.
 */
data class ScheduledServicePlanMetricWorkflow(
    var store: ScheduledServicePlanMetricStore,
    var validator: ScheduledServicePlanMetricValidator,
    var clock: Clock,
    var journal: MutableList<String>,
) {
    private fun note(message: String) {
        journal.add("${clock.now()} $message")
    }

    fun advance(code: String) {
        val item = store.find(code) ?: error("scheduled service plan metric $code not found")
        validator.requireValid(item)
        item.moveTo(ScheduledServicePlanMetricStatus.IN_REVIEW)
        store.save(item)
        note("$code is in review")
    }

    fun settle(code: String, reason: String) {
        val item = store.find(code) ?: error("scheduled service plan metric $code not found")
        if (item.isTerminal()) {
            note("$code already closed")
            return
        }
        item.moveTo(ScheduledServicePlanMetricStatus.ARCHIVED)
        store.save(item)
        note("$code is archived: $reason")
    }

    fun sweep(cutoff: Int): Int {
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

fun newScheduledServicePlanMetricWorkflow(store: ScheduledServicePlanMetricStore, clock: Clock): ScheduledServicePlanMetricWorkflow {
    return ScheduledServicePlanMetricWorkflow(
        store = store,
        validator = defaultScheduledServicePlanMetricValidator(),
        clock = clock,
        journal = mutableListOf<String>(),
    )
}
