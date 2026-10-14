package com.acme.transport.safety.cameraevent.budget

import com.acme.transport.safety.clock.Clock

/** Moves regional camera event budgets through their life cycle and keeps a journal of what it did. */
data class RegionalCameraEventBudgetWorkflow(
    var store: RegionalCameraEventBudgetStore,
    var validator: RegionalCameraEventBudgetValidator,
    var clock: Clock,
    var journal: MutableList<String>,
    var engine: RegionalCameraEventBudgetEngine,
) {
    private fun note(message: String) {
        journal.add("${clock.now()} $message")
    }

    fun open(code: String) {
        val item = store.find(code) ?: error("regional camera event budget $code not found")
        validator.requireValid(item)
        item.moveTo(RegionalCameraEventBudgetStatus.QUEUED)
        store.save(item)
        note("$code is queued")
    }

    fun complete(code: String, reason: String) {
        val item = store.find(code) ?: error("regional camera event budget $code not found")
        if (item.isTerminal()) {
            note("$code already closed")
            return
        }
        item.moveTo(RegionalCameraEventBudgetStatus.FAILED)
        store.save(item)
        note("$code is failed: $reason")
    }

    fun score(code: String): Double {
        val item = store.find(code) ?: error("regional camera event budget $code not found")
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

fun newRegionalCameraEventBudgetWorkflow(store: RegionalCameraEventBudgetStore, clock: Clock): RegionalCameraEventBudgetWorkflow {
    return RegionalCameraEventBudgetWorkflow(
        store = store,
        validator = defaultRegionalCameraEventBudgetValidator(),
        clock = clock,
        journal = mutableListOf<String>(),
        engine = defaultRegionalCameraEventBudgetEngine(),
    )
}
