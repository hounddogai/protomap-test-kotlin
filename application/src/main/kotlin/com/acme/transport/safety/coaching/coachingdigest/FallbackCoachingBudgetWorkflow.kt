package com.acme.transport.safety.coaching.coachingdigest

import com.acme.transport.safety.clock.Clock

/** Moves fallback coaching budgets through their life cycle and keeps a journal of what it did. */
data class FallbackCoachingBudgetManager(
    var store: FallbackCoachingBudgetStore,
    var validator: FallbackCoachingBudgetValidator,
    var clock: Clock,
    var journal: MutableList<String>,
    var engine: FallbackCoachingBudgetEngine,
) {
    private fun note(message: String) {
        journal.add("${clock.now()} $message")
    }

    fun submit(code: String) {
        val item = store.find(code) ?: error("fallback coaching budget $code not found")
        validator.requireValid(item)
        item.moveTo(FallbackCoachingBudgetStatus.WARMING)
        store.save(item)
        note("$code is warming")
    }

    fun complete(code: String, reason: String) {
        val item = store.find(code) ?: error("fallback coaching budget $code not found")
        if (item.isTerminal()) {
            note("$code already closed")
            return
        }
        item.moveTo(FallbackCoachingBudgetStatus.STOPPED)
        store.save(item)
        note("$code is stopped: $reason")
    }

    fun score(code: String): Double {
        val item = store.find(code) ?: error("fallback coaching budget $code not found")
        val result = engine.run(item)
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

fun newFallbackCoachingBudgetManager(store: FallbackCoachingBudgetStore, clock: Clock): FallbackCoachingBudgetManager {
    return FallbackCoachingBudgetManager(
        store = store,
        validator = defaultFallbackCoachingBudgetValidator(),
        clock = clock,
        journal = mutableListOf<String>(),
        engine = defaultFallbackCoachingBudgetEngine(),
    )
}
