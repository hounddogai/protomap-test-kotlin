package com.acme.transport.fleetops.tire.tirebudget

import com.acme.transport.fleetops.clock.Clock

/** Moves manual tire budgets through their life cycle and keeps a journal of what it did. */
data class ManualTireBudgetService(
    var store: ManualTireBudgetStore,
    var validator: ManualTireBudgetValidator,
    var clock: Clock,
    var journal: MutableList<String>,
    var engine: ManualTireBudgetEngine,
) {
    private fun note(message: String) {
        journal.add("${clock.now()} $message")
    }

    fun start(code: String) {
        val item = store.find(code) ?: error("manual tire budget $code not found")
        validator.requireValid(item)
        item.moveTo(ManualTireBudgetStatus.WARMING)
        store.save(item)
        note("$code is warming")
    }

    fun close(code: String, reason: String) {
        val item = store.find(code) ?: error("manual tire budget $code not found")
        if (item.isTerminal()) {
            note("$code already closed")
            return
        }
        item.moveTo(ManualTireBudgetStatus.STOPPED)
        store.save(item)
        note("$code is stopped: $reason")
    }

    fun score(code: String): Double {
        val item = store.find(code) ?: error("manual tire budget $code not found")
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
            if (item.createdAt > cutoff) {
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

fun newManualTireBudgetService(store: ManualTireBudgetStore, clock: Clock): ManualTireBudgetService {
    return ManualTireBudgetService(
        store = store,
        validator = defaultManualTireBudgetValidator(),
        clock = clock,
        journal = mutableListOf<String>(),
        engine = defaultManualTireBudgetEngine(),
    )
}
