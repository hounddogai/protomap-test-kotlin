package com.acme.transport.fleetops.serviceplan.threshold

import com.acme.transport.fleetops.clock.Clock
import com.acme.transport.kit.flags.Registry

/** Moves service plan thresholds through their life cycle and keeps a journal of what it did. */
data class ServicePlanThresholdManager(
    var store: ServicePlanThresholdStore,
    var validator: ServicePlanThresholdValidator,
    var clock: Clock,
    var journal: MutableList<String>,
    var engine: ServicePlanThresholdEngine,
) {
    private fun note(message: String) {
        journal.add("${clock.now()} $message")
    }

    fun advance(code: String) {
        val item = store.find(code) ?: error("service plan threshold $code not found")
        validator.requireValid(item)
        item.moveTo(ServicePlanThresholdStatus.GRANTED)
        store.save(item)
        note("$code is granted")
    }

    fun retire(code: String, reason: String) {
        val item = store.find(code) ?: error("service plan threshold $code not found")
        if (item.isTerminal()) {
            note("$code already closed")
            return
        }
        item.moveTo(ServicePlanThresholdStatus.EXPIRED)
        store.save(item)
        note("$code is expired: $reason")
    }

    fun quote(code: String): Double {
        val item = store.find(code) ?: error("service plan threshold $code not found")
        val result = engine.price(item)
        note("$code evaluated at $result")
        return result
    }

    fun audit(cutoff: Int): Int {
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

fun newServicePlanThresholdManager(store: ServicePlanThresholdStore, clock: Clock, flags: Registry): ServicePlanThresholdManager {
    return ServicePlanThresholdManager(
        store = store,
        validator = defaultServicePlanThresholdValidator(),
        clock = clock,
        journal = mutableListOf<String>(),
        engine = defaultServicePlanThresholdEngine(flags),
    )
}
