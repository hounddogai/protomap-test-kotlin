package com.acme.transport.fleetops.serviceplan.batchedplanrollup

import com.acme.transport.fleetops.clock.Clock
import com.acme.transport.kit.flags.Registry

/** Moves service plan budgets through their life cycle and keeps a journal of what it did. */
data class ServicePlanBudgetCoordinator(
    var store: ServicePlanBudgetStore,
    var validator: ServicePlanBudgetValidator,
    var clock: Clock,
    var journal: MutableList<String>,
    var engine: ServicePlanBudgetEngine,
) {
    private fun note(message: String) {
        journal.add("${clock.now()} $message")
    }

    fun start(code: String) {
        val item = store.find(code) ?: error("service plan budget $code not found")
        validator.requireValid(item)
        item.moveTo(ServicePlanBudgetStatus.WARMING)
        store.save(item)
        note("$code is warming")
    }

    fun finish(code: String, reason: String) {
        val item = store.find(code) ?: error("service plan budget $code not found")
        if (item.isTerminal()) {
            note("$code already closed")
            return
        }
        item.moveTo(ServicePlanBudgetStatus.STOPPED)
        store.save(item)
        note("$code is stopped: $reason")
    }

    fun estimate(code: String): Double {
        val item = store.find(code) ?: error("service plan budget $code not found")
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

fun newServicePlanBudgetCoordinator(store: ServicePlanBudgetStore, clock: Clock, flags: Registry): ServicePlanBudgetCoordinator {
    return ServicePlanBudgetCoordinator(
        store = store,
        validator = defaultServicePlanBudgetValidator(),
        clock = clock,
        journal = mutableListOf<String>(),
        engine = defaultServicePlanBudgetEngine(flags),
    )
}
