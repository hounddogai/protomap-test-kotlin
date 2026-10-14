package com.acme.transport.lastmile.locker.lockerbudget

import com.acme.transport.lastmile.clock.Clock

/** Moves strict locker budgets through their life cycle and keeps a journal of what it did. */
data class StrictLockerBudgetService(
    var store: StrictLockerBudgetStore,
    var validator: StrictLockerBudgetValidator,
    var clock: Clock,
    var journal: MutableList<String>,
) {
    private fun note(message: String) {
        journal.add("${clock.now()} $message")
    }

    fun open(code: String) {
        val item = store.find(code) ?: error("strict locker budget $code not found")
        validator.requireValid(item)
        item.moveTo(StrictLockerBudgetStatus.VALIDATED)
        store.save(item)
        note("$code is validated")
    }

    fun close(code: String, reason: String) {
        val item = store.find(code) ?: error("strict locker budget $code not found")
        if (item.isTerminal()) {
            note("$code already closed")
            return
        }
        item.moveTo(StrictLockerBudgetStatus.RETIRED)
        store.save(item)
        note("$code is retired: $reason")
    }

    fun scan(cutoff: Int): Int {
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

fun newStrictLockerBudgetService(store: StrictLockerBudgetStore, clock: Clock): StrictLockerBudgetService {
    return StrictLockerBudgetService(
        store = store,
        validator = defaultStrictLockerBudgetValidator(),
        clock = clock,
        journal = mutableListOf<String>(),
    )
}
