package com.acme.transport.safety.violation.metric

import com.acme.transport.kit.flags.Registry
import com.acme.transport.safety.clock.Clock

/** Moves rolling violation budgets through their life cycle and keeps a journal of what it did. */
data class RollingViolationBudgetWorkflow(
    var store: RollingViolationBudgetStore,
    var validator: RollingViolationBudgetValidator,
    var clock: Clock,
    var journal: MutableList<String>,
    var engine: RollingViolationBudgetEngine,
) {
    private fun note(message: String) {
        journal.add("${clock.now()} $message")
    }

    fun advance(code: String) {
        val item = store.find(code) ?: error("rolling violation budget $code not found")
        validator.requireValid(item)
        item.moveTo(RollingViolationBudgetStatus.APPROVED)
        store.save(item)
        note("$code is approved")
    }

    fun finish(code: String, reason: String) {
        val item = store.find(code) ?: error("rolling violation budget $code not found")
        if (item.isTerminal()) {
            note("$code already closed")
            return
        }
        item.moveTo(RollingViolationBudgetStatus.WITHDRAWN)
        store.save(item)
        note("$code is withdrawn: $reason")
    }

    fun quote(code: String): Double {
        val item = store.find(code) ?: error("rolling violation budget $code not found")
        val result = engine.run(item)
        note("$code evaluated at $result")
        return result
    }

    fun scan(cutoff: Int): Int {
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

fun newRollingViolationBudgetWorkflow(store: RollingViolationBudgetStore, clock: Clock, flags: Registry): RollingViolationBudgetWorkflow {
    return RollingViolationBudgetWorkflow(
        store = store,
        validator = defaultRollingViolationBudgetValidator(),
        clock = clock,
        journal = mutableListOf<String>(),
        engine = defaultRollingViolationBudgetEngine(flags),
    )
}
