package com.acme.transport.fleetops.serviceplan.request

import com.acme.transport.fleetops.clock.Clock

/** Moves service plan quotas through their life cycle and keeps a journal of what it did. */
data class ServicePlanQuotaWorkflow(
    var store: ServicePlanQuotaStore,
    var validator: ServicePlanQuotaValidator,
    var clock: Clock,
    var journal: MutableList<String>,
    var engine: ServicePlanQuotaEngine,
) {
    private fun note(message: String) {
        journal.add("${clock.now()} $message")
    }

    fun start(code: String) {
        val item = store.find(code) ?: error("service plan quota $code not found")
        validator.requireValid(item)
        item.moveTo(ServicePlanQuotaStatus.SCHEDULED)
        store.save(item)
        note("$code is scheduled")
    }

    fun settle(code: String, reason: String) {
        val item = store.find(code) ?: error("service plan quota $code not found")
        if (item.isTerminal()) {
            note("$code already closed")
            return
        }
        item.moveTo(ServicePlanQuotaStatus.CANCELLED)
        store.save(item)
        note("$code is cancelled: $reason")
    }

    fun estimate(code: String): Double {
        val item = store.find(code) ?: error("service plan quota $code not found")
        val result = engine.compute(item)
        note("$code evaluated at $result")
        return result
    }

    fun reviewStale(cutoff: Int): Int {
        var changed = 0
        for (item in store.list()) {
            if (item.isTerminal()) {
                continue
            }
            if (item.startsAt > cutoff) {
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

fun newServicePlanQuotaWorkflow(store: ServicePlanQuotaStore, clock: Clock): ServicePlanQuotaWorkflow {
    return ServicePlanQuotaWorkflow(
        store = store,
        validator = defaultServicePlanQuotaValidator(),
        clock = clock,
        journal = mutableListOf<String>(),
        engine = defaultServicePlanQuotaEngine(),
    )
}
