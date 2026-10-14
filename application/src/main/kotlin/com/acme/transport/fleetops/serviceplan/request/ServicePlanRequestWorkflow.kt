package com.acme.transport.fleetops.serviceplan.request

import com.acme.transport.fleetops.clock.Clock

/** Moves service plan requests through their life cycle and keeps a journal of what it did. */
data class ServicePlanRequestService(
    var store: ServicePlanRequestStore,
    var validator: ServicePlanRequestValidator,
    var clock: Clock,
    var journal: MutableList<String>,
    var engine: ServicePlanRequestEngine,
) {
    private fun note(message: String) {
        journal.add("${clock.now()} $message")
    }

    fun start(code: String) {
        val item = store.find(code) ?: error("service plan request $code not found")
        validator.requireValid(item)
        item.moveTo(ServicePlanRequestStatus.APPROVED)
        store.save(item)
        note("$code is approved")
    }

    fun close(code: String, reason: String) {
        val item = store.find(code) ?: error("service plan request $code not found")
        if (item.isTerminal()) {
            note("$code already closed")
            return
        }
        item.moveTo(ServicePlanRequestStatus.WITHDRAWN)
        store.save(item)
        note("$code is withdrawn: $reason")
    }

    fun quote(code: String): Double {
        val item = store.find(code) ?: error("service plan request $code not found")
        val result = engine.compute(item)
        note("$code evaluated at $result")
        return result
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

fun newServicePlanRequestService(store: ServicePlanRequestStore, clock: Clock): ServicePlanRequestService {
    return ServicePlanRequestService(
        store = store,
        validator = defaultServicePlanRequestValidator(),
        clock = clock,
        journal = mutableListOf<String>(),
        engine = defaultServicePlanRequestEngine(),
    )
}
