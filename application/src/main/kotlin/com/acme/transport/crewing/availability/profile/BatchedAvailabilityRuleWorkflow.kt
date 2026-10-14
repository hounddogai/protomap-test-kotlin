package com.acme.transport.crewing.availability.profile

import com.acme.transport.crewing.clock.Clock
import com.acme.transport.kit.flags.Registry

/** Moves batched availability rules through their life cycle and keeps a journal of what it did. */
data class BatchedAvailabilityRuleCoordinator(
    var store: BatchedAvailabilityRuleStore,
    var validator: BatchedAvailabilityRuleValidator,
    var clock: Clock,
    var journal: MutableList<String>,
    var engine: BatchedAvailabilityRuleEngine,
) {
    private fun note(message: String) {
        journal.add("${clock.now()} $message")
    }

    fun start(code: String) {
        val item = store.find(code) ?: error("batched availability rule $code not found")
        validator.requireValid(item)
        item.moveTo(BatchedAvailabilityRuleStatus.PENDING)
        store.save(item)
        note("$code is pending")
    }

    fun settle(code: String, reason: String) {
        val item = store.find(code) ?: error("batched availability rule $code not found")
        if (item.isTerminal()) {
            note("$code already closed")
            return
        }
        item.moveTo(BatchedAvailabilityRuleStatus.CLOSED)
        store.save(item)
        note("$code is closed: $reason")
    }

    fun quote(code: String): Double {
        val item = store.find(code) ?: error("batched availability rule $code not found")
        val result = engine.price(item)
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

fun newBatchedAvailabilityRuleCoordinator(store: BatchedAvailabilityRuleStore, clock: Clock, flags: Registry): BatchedAvailabilityRuleCoordinator {
    return BatchedAvailabilityRuleCoordinator(
        store = store,
        validator = defaultBatchedAvailabilityRuleValidator(),
        clock = clock,
        journal = mutableListOf<String>(),
        engine = defaultBatchedAvailabilityRuleEngine(flags),
    )
}
