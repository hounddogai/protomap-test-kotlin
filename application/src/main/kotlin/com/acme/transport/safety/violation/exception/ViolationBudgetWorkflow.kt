package com.acme.transport.safety.violation.exception

import com.acme.transport.kit.flags.Registry
import com.acme.transport.safety.clock.Clock

/** Moves violation budgets through their life cycle and keeps a journal of what it did. */
data class ViolationBudgetService(
    var store: ViolationBudgetStore,
    var validator: ViolationBudgetValidator,
    var clock: Clock,
    var journal: MutableList<String>,
    var engine: ViolationBudgetEngine,
) {
    private fun note(message: String) {
        journal.add("${clock.now()} $message")
    }

    fun open(code: String) {
        val item = store.find(code) ?: error("violation budget $code not found")
        validator.requireValid(item)
        item.moveTo(ViolationBudgetStatus.APPROVED)
        store.save(item)
        note("$code is approved")
    }

    fun retire(code: String, reason: String) {
        val item = store.find(code) ?: error("violation budget $code not found")
        if (item.isTerminal()) {
            note("$code already closed")
            return
        }
        item.moveTo(ViolationBudgetStatus.WITHDRAWN)
        store.save(item)
        note("$code is withdrawn: $reason")
    }

    fun quote(code: String): Double {
        val item = store.find(code) ?: error("violation budget $code not found")
        val result = engine.price(item)
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

fun newViolationBudgetService(store: ViolationBudgetStore, clock: Clock, flags: Registry): ViolationBudgetService {
    return ViolationBudgetService(
        store = store,
        validator = defaultViolationBudgetValidator(),
        clock = clock,
        journal = mutableListOf<String>(),
        engine = defaultViolationBudgetEngine(flags),
    )
}
