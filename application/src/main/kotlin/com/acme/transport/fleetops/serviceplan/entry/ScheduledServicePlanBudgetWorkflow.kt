package com.acme.transport.fleetops.serviceplan.entry

import com.acme.transport.fleetops.clock.Clock

/**
 * Moves scheduled service plan budgets through their life cycle and keeps a journal of what it did.
 */
data class ScheduledServicePlanBudgetService(
    var store: ScheduledServicePlanBudgetStore,
    var validator: ScheduledServicePlanBudgetValidator,
    var clock: Clock,
    var journal: MutableList<String>,
    var engine: ScheduledServicePlanBudgetEngine,
) {
    private fun note(message: String) {
        journal.add("${clock.now()} $message")
    }

    fun advance(code: String) {
        val item = store.find(code) ?: error("scheduled service plan budget $code not found")
        validator.requireValid(item)
        item.moveTo(ScheduledServicePlanBudgetStatus.IN_REVIEW)
        store.save(item)
        note("$code is in review")
    }

    fun finish(code: String, reason: String) {
        val item = store.find(code) ?: error("scheduled service plan budget $code not found")
        if (item.isTerminal()) {
            note("$code already closed")
            return
        }
        item.moveTo(ScheduledServicePlanBudgetStatus.ARCHIVED)
        store.save(item)
        note("$code is archived: $reason")
    }

    fun evaluate(code: String): Double {
        val item = store.find(code) ?: error("scheduled service plan budget $code not found")
        val result = engine.evaluate(item)
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

fun newScheduledServicePlanBudgetService(store: ScheduledServicePlanBudgetStore, clock: Clock): ScheduledServicePlanBudgetService {
    return ScheduledServicePlanBudgetService(
        store = store,
        validator = defaultScheduledServicePlanBudgetValidator(),
        clock = clock,
        journal = mutableListOf<String>(),
        engine = defaultScheduledServicePlanBudgetEngine(),
    )
}
