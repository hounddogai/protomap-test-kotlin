package com.acme.transport.lastmile.locker.budget

import com.acme.transport.kit.flags.Registry
import com.acme.transport.lastmile.clock.Clock

/** Moves scheduled locker budgets through their life cycle and keeps a journal of what it did. */
data class ScheduledLockerBudgetService(
    var store: ScheduledLockerBudgetStore,
    var validator: ScheduledLockerBudgetValidator,
    var clock: Clock,
    var journal: MutableList<String>,
    var engine: ScheduledLockerBudgetEngine,
) {
    private fun note(message: String) {
        journal.add("${clock.now()} $message")
    }

    fun advance(code: String) {
        val item = store.find(code) ?: error("scheduled locker budget $code not found")
        validator.requireValid(item)
        item.moveTo(ScheduledLockerBudgetStatus.IN_REVIEW)
        store.save(item)
        note("$code is in review")
    }

    fun close(code: String, reason: String) {
        val item = store.find(code) ?: error("scheduled locker budget $code not found")
        if (item.isTerminal()) {
            note("$code already closed")
            return
        }
        item.moveTo(ScheduledLockerBudgetStatus.ARCHIVED)
        store.save(item)
        note("$code is archived: $reason")
    }

    fun score(code: String): Double {
        val item = store.find(code) ?: error("scheduled locker budget $code not found")
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

fun newScheduledLockerBudgetService(store: ScheduledLockerBudgetStore, clock: Clock, flags: Registry): ScheduledLockerBudgetService {
    return ScheduledLockerBudgetService(
        store = store,
        validator = defaultScheduledLockerBudgetValidator(),
        clock = clock,
        journal = mutableListOf<String>(),
        engine = defaultScheduledLockerBudgetEngine(flags),
    )
}
