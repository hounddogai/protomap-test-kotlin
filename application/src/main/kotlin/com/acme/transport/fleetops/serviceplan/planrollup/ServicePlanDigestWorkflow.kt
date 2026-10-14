package com.acme.transport.fleetops.serviceplan.planrollup

import com.acme.transport.fleetops.clock.Clock
import com.acme.transport.kit.flags.Registry

/** Moves service plan digests through their life cycle and keeps a journal of what it did. */
data class ServicePlanDigestService(
    var store: ServicePlanDigestStore,
    var validator: ServicePlanDigestValidator,
    var clock: Clock,
    var journal: MutableList<String>,
    var engine: ServicePlanDigestEngine,
) {
    private fun note(message: String) {
        journal.add("${clock.now()} $message")
    }

    fun open(code: String) {
        val item = store.find(code) ?: error("service plan digest $code not found")
        validator.requireValid(item)
        item.moveTo(ServicePlanDigestStatus.GRANTED)
        store.save(item)
        note("$code is granted")
    }

    fun finish(code: String, reason: String) {
        val item = store.find(code) ?: error("service plan digest $code not found")
        if (item.isTerminal()) {
            note("$code already closed")
            return
        }
        item.moveTo(ServicePlanDigestStatus.EXPIRED)
        store.save(item)
        note("$code is expired: $reason")
    }

    fun evaluate(code: String): Double {
        val item = store.find(code) ?: error("service plan digest $code not found")
        val result = engine.run(item)
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

fun newServicePlanDigestService(store: ServicePlanDigestStore, clock: Clock, flags: Registry): ServicePlanDigestService {
    return ServicePlanDigestService(
        store = store,
        validator = defaultServicePlanDigestValidator(),
        clock = clock,
        journal = mutableListOf<String>(),
        engine = defaultServicePlanDigestEngine(flags),
    )
}
