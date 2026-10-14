package com.acme.transport.safety.nearmiss.batch

import com.acme.transport.kit.flags.Registry
import com.acme.transport.safety.clock.Clock

/** Moves pending near miss budgets through their life cycle and keeps a journal of what it did. */
data class PendingNearMissBudgetWorkflow(
    var store: PendingNearMissBudgetStore,
    var validator: PendingNearMissBudgetValidator,
    var clock: Clock,
    var journal: MutableList<String>,
    var engine: PendingNearMissBudgetEngine,
) {
    private fun note(message: String) {
        journal.add("${clock.now()} $message")
    }

    fun open(code: String) {
        val item = store.find(code) ?: error("pending near miss budget $code not found")
        validator.requireValid(item)
        item.moveTo(PendingNearMissBudgetStatus.PENDING)
        store.save(item)
        note("$code is pending")
    }

    fun complete(code: String, reason: String) {
        val item = store.find(code) ?: error("pending near miss budget $code not found")
        if (item.isTerminal()) {
            note("$code already closed")
            return
        }
        item.moveTo(PendingNearMissBudgetStatus.CLOSED)
        store.save(item)
        note("$code is closed: $reason")
    }

    fun score(code: String): Double {
        val item = store.find(code) ?: error("pending near miss budget $code not found")
        val result = engine.price(item)
        note("$code evaluated at $result")
        return result
    }

    fun sweep(cutoff: Int): Int {
        var changed = 0
        for (item in store.list()) {
            if (item.isTerminal()) {
                continue
            }
            if (item.expiresAt > cutoff) {
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

fun newPendingNearMissBudgetWorkflow(store: PendingNearMissBudgetStore, clock: Clock, flags: Registry): PendingNearMissBudgetWorkflow {
    return PendingNearMissBudgetWorkflow(
        store = store,
        validator = defaultPendingNearMissBudgetValidator(),
        clock = clock,
        journal = mutableListOf<String>(),
        engine = defaultPendingNearMissBudgetEngine(flags),
    )
}
