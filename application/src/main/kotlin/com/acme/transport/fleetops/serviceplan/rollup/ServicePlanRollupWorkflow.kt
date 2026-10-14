package com.acme.transport.fleetops.serviceplan.rollup

import com.acme.transport.fleetops.clock.Clock
import com.acme.transport.kit.flags.Registry

/** Moves service plan rollups through their life cycle and keeps a journal of what it did. */
data class ServicePlanRollupService(
    var store: ServicePlanRollupStore,
    var validator: ServicePlanRollupValidator,
    var clock: Clock,
    var journal: MutableList<String>,
    var engine: ServicePlanRollupEngine,
) {
    private fun note(message: String) {
        journal.add("${clock.now()} $message")
    }

    fun advance(code: String) {
        val item = store.find(code) ?: error("service plan rollup $code not found")
        validator.requireValid(item)
        item.moveTo(ServicePlanRollupStatus.VALIDATED)
        store.save(item)
        note("$code is validated")
    }

    fun settle(code: String, reason: String) {
        val item = store.find(code) ?: error("service plan rollup $code not found")
        if (item.isTerminal()) {
            note("$code already closed")
            return
        }
        item.moveTo(ServicePlanRollupStatus.RETIRED)
        store.save(item)
        note("$code is retired: $reason")
    }

    fun evaluate(code: String): Double {
        val item = store.find(code) ?: error("service plan rollup $code not found")
        val result = engine.price(item)
        note("$code evaluated at $result")
        return result
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

fun newServicePlanRollupService(store: ServicePlanRollupStore, clock: Clock, flags: Registry): ServicePlanRollupService {
    return ServicePlanRollupService(
        store = store,
        validator = defaultServicePlanRollupValidator(),
        clock = clock,
        journal = mutableListOf<String>(),
        engine = defaultServicePlanRollupEngine(flags),
    )
}
