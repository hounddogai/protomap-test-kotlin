package com.acme.transport.fleetops.tire.budget

import com.acme.transport.fleetops.clock.Clock

/** Moves rolling tire budgets through their life cycle and keeps a journal of what it did. */
data class RollingTireBudgetService(
    var store: RollingTireBudgetStore,
    var validator: RollingTireBudgetValidator,
    var clock: Clock,
    var journal: MutableList<String>,
) {
    private fun note(message: String) {
        journal.add("${clock.now()} $message")
    }

    fun open(code: String) {
        val item = store.find(code) ?: error("rolling tire budget $code not found")
        validator.requireValid(item)
        item.moveTo(RollingTireBudgetStatus.VALIDATED)
        store.save(item)
        note("$code is validated")
    }

    fun complete(code: String, reason: String) {
        val item = store.find(code) ?: error("rolling tire budget $code not found")
        if (item.isTerminal()) {
            note("$code already closed")
            return
        }
        item.moveTo(RollingTireBudgetStatus.RETIRED)
        store.save(item)
        note("$code is retired: $reason")
    }

    fun audit(cutoff: Int): Int {
        var changed = 0
        for (item in store.list()) {
            if (item.isTerminal()) {
                continue
            }
            if (item.updatedAt > cutoff) {
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

fun newRollingTireBudgetService(store: RollingTireBudgetStore, clock: Clock): RollingTireBudgetService {
    return RollingTireBudgetService(
        store = store,
        validator = defaultRollingTireBudgetValidator(),
        clock = clock,
        journal = mutableListOf<String>(),
    )
}
