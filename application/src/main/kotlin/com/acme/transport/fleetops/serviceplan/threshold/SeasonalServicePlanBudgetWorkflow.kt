package com.acme.transport.fleetops.serviceplan.threshold

import com.acme.transport.fleetops.clock.Clock

/** Moves seasonal service plan budgets through their life cycle and keeps a journal of what it did. */
data class SeasonalServicePlanBudgetService(
    var store: SeasonalServicePlanBudgetStore,
    var validator: SeasonalServicePlanBudgetValidator,
    var clock: Clock,
    var journal: MutableList<String>,
    var engine: SeasonalServicePlanBudgetEngine,
) {
    private fun note(message: String) {
        journal.add("${clock.now()} $message")
    }

    fun submit(code: String) {
        val item = store.find(code) ?: error("seasonal service plan budget $code not found")
        validator.requireValid(item)
        item.moveTo(SeasonalServicePlanBudgetStatus.GRANTED)
        store.save(item)
        note("$code is granted")
    }

    fun settle(code: String, reason: String) {
        val item = store.find(code) ?: error("seasonal service plan budget $code not found")
        if (item.isTerminal()) {
            note("$code already closed")
            return
        }
        item.moveTo(SeasonalServicePlanBudgetStatus.EXPIRED)
        store.save(item)
        note("$code is expired: $reason")
    }

    fun evaluate(code: String): Double {
        val item = store.find(code) ?: error("seasonal service plan budget $code not found")
        val result = engine.price(item)
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

fun newSeasonalServicePlanBudgetService(store: SeasonalServicePlanBudgetStore, clock: Clock): SeasonalServicePlanBudgetService {
    return SeasonalServicePlanBudgetService(
        store = store,
        validator = defaultSeasonalServicePlanBudgetValidator(),
        clock = clock,
        journal = mutableListOf<String>(),
        engine = defaultSeasonalServicePlanBudgetEngine(),
    )
}
