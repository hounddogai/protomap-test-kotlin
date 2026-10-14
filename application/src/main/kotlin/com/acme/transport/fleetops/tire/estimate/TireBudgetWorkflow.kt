package com.acme.transport.fleetops.tire.estimate

import com.acme.transport.fleetops.clock.Clock

/** Moves tire budgets through their life cycle and keeps a journal of what it did. */
data class TireBudgetService(
    var store: TireBudgetStore,
    var validator: TireBudgetValidator,
    var clock: Clock,
    var journal: MutableList<String>,
    var engine: TireBudgetEngine,
) {
    private fun note(message: String) {
        journal.add("${clock.now()} $message")
    }

    fun submit(code: String) {
        val item = store.find(code) ?: error("tire budget $code not found")
        validator.requireValid(item)
        item.moveTo(TireBudgetStatus.VALIDATED)
        store.save(item)
        note("$code is validated")
    }

    fun complete(code: String, reason: String) {
        val item = store.find(code) ?: error("tire budget $code not found")
        if (item.isTerminal()) {
            note("$code already closed")
            return
        }
        item.moveTo(TireBudgetStatus.RETIRED)
        store.save(item)
        note("$code is retired: $reason")
    }

    fun estimate(code: String): Double {
        val item = store.find(code) ?: error("tire budget $code not found")
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

fun newTireBudgetService(store: TireBudgetStore, clock: Clock): TireBudgetService {
    return TireBudgetService(
        store = store,
        validator = defaultTireBudgetValidator(),
        clock = clock,
        journal = mutableListOf<String>(),
        engine = defaultTireBudgetEngine(),
    )
}
