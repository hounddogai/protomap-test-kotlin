package com.acme.transport.safety.coaching.coachingmetric

import com.acme.transport.kit.flags.Registry
import com.acme.transport.safety.clock.Clock

/** Moves pending coaching budgets through their life cycle and keeps a journal of what it did. */
data class PendingCoachingBudgetCoordinator(
    var store: PendingCoachingBudgetStore,
    var validator: PendingCoachingBudgetValidator,
    var clock: Clock,
    var journal: MutableList<String>,
    var engine: PendingCoachingBudgetEngine,
) {
    private fun note(message: String) {
        journal.add("${clock.now()} $message")
    }

    fun advance(code: String) {
        val item = store.find(code) ?: error("pending coaching budget $code not found")
        validator.requireValid(item)
        item.moveTo(PendingCoachingBudgetStatus.WARMING)
        store.save(item)
        note("$code is warming")
    }

    fun complete(code: String, reason: String) {
        val item = store.find(code) ?: error("pending coaching budget $code not found")
        if (item.isTerminal()) {
            note("$code already closed")
            return
        }
        item.moveTo(PendingCoachingBudgetStatus.STOPPED)
        store.save(item)
        note("$code is stopped: $reason")
    }

    fun score(code: String): Double {
        val item = store.find(code) ?: error("pending coaching budget $code not found")
        val result = engine.run(item)
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

fun newPendingCoachingBudgetCoordinator(store: PendingCoachingBudgetStore, clock: Clock, flags: Registry): PendingCoachingBudgetCoordinator {
    return PendingCoachingBudgetCoordinator(
        store = store,
        validator = defaultPendingCoachingBudgetValidator(),
        clock = clock,
        journal = mutableListOf<String>(),
        engine = defaultPendingCoachingBudgetEngine(flags),
    )
}
