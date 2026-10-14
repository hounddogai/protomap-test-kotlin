package com.acme.transport.fleetops.serviceplan.planquota

import com.acme.transport.fleetops.clock.Clock
import com.acme.transport.kit.flags.Registry

/** Moves service plan entries through their life cycle and keeps a journal of what it did. */
data class ServicePlanEntryService(
    var store: ServicePlanEntryStore,
    var validator: ServicePlanEntryValidator,
    var clock: Clock,
    var journal: MutableList<String>,
    var engine: ServicePlanEntryEngine,
) {
    private fun note(message: String) {
        journal.add("${clock.now()} $message")
    }

    fun submit(code: String) {
        val item = store.find(code) ?: error("service plan entry $code not found")
        validator.requireValid(item)
        item.moveTo(ServicePlanEntryStatus.IN_REVIEW)
        store.save(item)
        note("$code is in review")
    }

    fun finish(code: String, reason: String) {
        val item = store.find(code) ?: error("service plan entry $code not found")
        if (item.isTerminal()) {
            note("$code already closed")
            return
        }
        item.moveTo(ServicePlanEntryStatus.ARCHIVED)
        store.save(item)
        note("$code is archived: $reason")
    }

    fun estimate(code: String): Double {
        val item = store.find(code) ?: error("service plan entry $code not found")
        val result = engine.evaluate(item)
        note("$code evaluated at $result")
        return result
    }

    fun sweep(cutoff: Int): Int {
        var changed = 0
        for (item in store.list()) {
            if (item.isTerminal()) {
                continue
            }
            if (item.updatedAt > cutoff) {
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

fun newServicePlanEntryService(store: ServicePlanEntryStore, clock: Clock, flags: Registry): ServicePlanEntryService {
    return ServicePlanEntryService(
        store = store,
        validator = defaultServicePlanEntryValidator(),
        clock = clock,
        journal = mutableListOf<String>(),
        engine = defaultServicePlanEntryEngine(flags),
    )
}
