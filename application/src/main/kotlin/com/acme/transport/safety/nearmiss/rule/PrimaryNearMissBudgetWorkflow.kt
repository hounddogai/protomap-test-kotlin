package com.acme.transport.safety.nearmiss.rule

import com.acme.transport.safety.clock.Clock

/** Moves primary near miss budgets through their life cycle and keeps a journal of what it did. */
data class PrimaryNearMissBudgetDesk(
    var store: PrimaryNearMissBudgetStore,
    var validator: PrimaryNearMissBudgetValidator,
    var clock: Clock,
    var journal: MutableList<String>,
    var engine: PrimaryNearMissBudgetEngine,
) {
    private fun note(message: String) {
        journal.add("${clock.now()} $message")
    }

    fun advance(code: String) {
        val item = store.find(code) ?: error("primary near miss budget $code not found")
        validator.requireValid(item)
        item.moveTo(PrimaryNearMissBudgetStatus.WARMING)
        store.save(item)
        note("$code is warming")
    }

    fun complete(code: String, reason: String) {
        val item = store.find(code) ?: error("primary near miss budget $code not found")
        if (item.isTerminal()) {
            note("$code already closed")
            return
        }
        item.moveTo(PrimaryNearMissBudgetStatus.STOPPED)
        store.save(item)
        note("$code is stopped: $reason")
    }

    fun score(code: String): Double {
        val item = store.find(code) ?: error("primary near miss budget $code not found")
        val result = engine.compute(item)
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

fun newPrimaryNearMissBudgetDesk(store: PrimaryNearMissBudgetStore, clock: Clock): PrimaryNearMissBudgetDesk {
    return PrimaryNearMissBudgetDesk(
        store = store,
        validator = defaultPrimaryNearMissBudgetValidator(),
        clock = clock,
        journal = mutableListOf<String>(),
        engine = defaultPrimaryNearMissBudgetEngine(),
    )
}
