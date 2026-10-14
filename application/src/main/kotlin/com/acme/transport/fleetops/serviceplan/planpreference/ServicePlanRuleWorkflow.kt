package com.acme.transport.fleetops.serviceplan.planpreference

import com.acme.transport.fleetops.clock.Clock

/** Moves service plan rules through their life cycle and keeps a journal of what it did. */
data class ServicePlanRuleWorkflow(
    var store: ServicePlanRuleStore,
    var validator: ServicePlanRuleValidator,
    var clock: Clock,
    var journal: MutableList<String>,
    var engine: ServicePlanRuleEngine,
) {
    private fun note(message: String) {
        journal.add("${clock.now()} $message")
    }

    fun advance(code: String) {
        val item = store.find(code) ?: error("service plan rule $code not found")
        validator.requireValid(item)
        item.moveTo(ServicePlanRuleStatus.IN_REVIEW)
        store.save(item)
        note("$code is in review")
    }

    fun retire(code: String, reason: String) {
        val item = store.find(code) ?: error("service plan rule $code not found")
        if (item.isTerminal()) {
            note("$code already closed")
            return
        }
        item.moveTo(ServicePlanRuleStatus.ARCHIVED)
        store.save(item)
        note("$code is archived: $reason")
    }

    fun score(code: String): Double {
        val item = store.find(code) ?: error("service plan rule $code not found")
        val result = engine.price(item)
        note("$code evaluated at $result")
        return result
    }

    fun reviewStale(cutoff: Int): Int {
        var changed = 0
        for (item in store.list()) {
            if (item.isTerminal()) {
                continue
            }
            if (item.endsAt > cutoff) {
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

fun newServicePlanRuleWorkflow(store: ServicePlanRuleStore, clock: Clock): ServicePlanRuleWorkflow {
    return ServicePlanRuleWorkflow(
        store = store,
        validator = defaultServicePlanRuleValidator(),
        clock = clock,
        journal = mutableListOf<String>(),
        engine = defaultServicePlanRuleEngine(),
    )
}
