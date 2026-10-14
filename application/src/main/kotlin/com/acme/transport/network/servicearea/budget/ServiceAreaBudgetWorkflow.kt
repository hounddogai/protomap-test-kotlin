package com.acme.transport.network.servicearea.budget

import com.acme.transport.network.clock.Clock

/** Moves service area budgets through their life cycle and keeps a journal of what it did. */
data class ServiceAreaBudgetService(
    var store: ServiceAreaBudgetStore,
    var validator: ServiceAreaBudgetValidator,
    var clock: Clock,
    var journal: MutableList<String>,
) {
    private fun note(message: String) {
        journal.add("${clock.now()} $message")
    }

    fun open(code: String) {
        val item = store.find(code) ?: error("service area budget $code not found")
        validator.requireValid(item)
        item.moveTo(ServiceAreaBudgetStatus.PENDING)
        store.save(item)
        note("$code is pending")
    }

    fun retire(code: String, reason: String) {
        val item = store.find(code) ?: error("service area budget $code not found")
        if (item.isTerminal()) {
            note("$code already closed")
            return
        }
        item.moveTo(ServiceAreaBudgetStatus.CLOSED)
        store.save(item)
        note("$code is closed: $reason")
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

fun newServiceAreaBudgetService(store: ServiceAreaBudgetStore, clock: Clock): ServiceAreaBudgetService {
    return ServiceAreaBudgetService(
        store = store,
        validator = defaultServiceAreaBudgetValidator(),
        clock = clock,
        journal = mutableListOf<String>(),
    )
}
