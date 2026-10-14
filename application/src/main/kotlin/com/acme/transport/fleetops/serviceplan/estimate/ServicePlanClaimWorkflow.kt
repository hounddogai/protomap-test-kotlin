package com.acme.transport.fleetops.serviceplan.estimate

import com.acme.transport.fleetops.clock.Clock
import com.acme.transport.kit.flags.Registry

/** Moves service plan claims through their life cycle and keeps a journal of what it did. */
data class ServicePlanClaimDesk(
    var store: ServicePlanClaimStore,
    var validator: ServicePlanClaimValidator,
    var clock: Clock,
    var journal: MutableList<String>,
    var engine: ServicePlanClaimEngine,
) {
    private fun note(message: String) {
        journal.add("${clock.now()} $message")
    }

    fun submit(code: String) {
        val item = store.find(code) ?: error("service plan claim $code not found")
        validator.requireValid(item)
        item.moveTo(ServicePlanClaimStatus.APPROVED)
        store.save(item)
        note("$code is approved")
    }

    fun complete(code: String, reason: String) {
        val item = store.find(code) ?: error("service plan claim $code not found")
        if (item.isTerminal()) {
            note("$code already closed")
            return
        }
        item.moveTo(ServicePlanClaimStatus.WITHDRAWN)
        store.save(item)
        note("$code is withdrawn: $reason")
    }

    fun evaluate(code: String): Double {
        val item = store.find(code) ?: error("service plan claim $code not found")
        val result = engine.evaluate(item)
        note("$code evaluated at $result")
        return result
    }

    fun scan(cutoff: Int): Int {
        var changed = 0
        for (item in store.list()) {
            if (item.isTerminal()) {
                continue
            }
            if (item.createdAt > cutoff) {
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

fun newServicePlanClaimDesk(store: ServicePlanClaimStore, clock: Clock, flags: Registry): ServicePlanClaimDesk {
    return ServicePlanClaimDesk(
        store = store,
        validator = defaultServicePlanClaimValidator(),
        clock = clock,
        journal = mutableListOf<String>(),
        engine = defaultServicePlanClaimEngine(flags),
    )
}
