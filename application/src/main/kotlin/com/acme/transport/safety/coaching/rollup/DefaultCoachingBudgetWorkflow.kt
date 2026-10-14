package com.acme.transport.safety.coaching.rollup

import com.acme.transport.safety.clock.Clock

/** Moves default coaching budgets through their life cycle and keeps a journal of what it did. */
data class DefaultCoachingBudgetDesk(
    var store: DefaultCoachingBudgetStore,
    var validator: DefaultCoachingBudgetValidator,
    var clock: Clock,
    var journal: MutableList<String>,
    var engine: DefaultCoachingBudgetEngine,
) {
    private fun note(message: String) {
        journal.add("${clock.now()} $message")
    }

    fun open(code: String) {
        val item = store.find(code) ?: error("default coaching budget $code not found")
        validator.requireValid(item)
        item.moveTo(DefaultCoachingBudgetStatus.WARMING)
        store.save(item)
        note("$code is warming")
    }

    fun close(code: String, reason: String) {
        val item = store.find(code) ?: error("default coaching budget $code not found")
        if (item.isTerminal()) {
            note("$code already closed")
            return
        }
        item.moveTo(DefaultCoachingBudgetStatus.STOPPED)
        store.save(item)
        note("$code is stopped: $reason")
    }

    fun score(code: String): Double {
        val item = store.find(code) ?: error("default coaching budget $code not found")
        val result = engine.evaluate(item)
        note("$code evaluated at $result")
        return result
    }

    fun reviewStale(cutoff: Int): Int {
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

fun newDefaultCoachingBudgetDesk(store: DefaultCoachingBudgetStore, clock: Clock): DefaultCoachingBudgetDesk {
    return DefaultCoachingBudgetDesk(
        store = store,
        validator = defaultDefaultCoachingBudgetValidator(),
        clock = clock,
        journal = mutableListOf<String>(),
        engine = defaultDefaultCoachingBudgetEngine(),
    )
}
