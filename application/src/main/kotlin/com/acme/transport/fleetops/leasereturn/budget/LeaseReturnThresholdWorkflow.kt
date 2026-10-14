package com.acme.transport.fleetops.leasereturn.budget

import com.acme.transport.fleetops.clock.Clock
import com.acme.transport.kit.flags.Registry

/** Moves lease return thresholds through their life cycle and keeps a journal of what it did. */
data class LeaseReturnThresholdCoordinator(
    var store: LeaseReturnThresholdStore,
    var validator: LeaseReturnThresholdValidator,
    var clock: Clock,
    var journal: MutableList<String>,
    var engine: LeaseReturnThresholdEngine,
) {
    private fun note(message: String) {
        journal.add("${clock.now()} $message")
    }

    fun advance(code: String) {
        val item = store.find(code) ?: error("lease return threshold $code not found")
        validator.requireValid(item)
        item.moveTo(LeaseReturnThresholdStatus.APPROVED)
        store.save(item)
        note("$code is approved")
    }

    fun retire(code: String, reason: String) {
        val item = store.find(code) ?: error("lease return threshold $code not found")
        if (item.isTerminal()) {
            note("$code already closed")
            return
        }
        item.moveTo(LeaseReturnThresholdStatus.WITHDRAWN)
        store.save(item)
        note("$code is withdrawn: $reason")
    }

    fun score(code: String): Double {
        val item = store.find(code) ?: error("lease return threshold $code not found")
        val result = engine.evaluate(item)
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

fun newLeaseReturnThresholdCoordinator(store: LeaseReturnThresholdStore, clock: Clock, flags: Registry): LeaseReturnThresholdCoordinator {
    return LeaseReturnThresholdCoordinator(
        store = store,
        validator = defaultLeaseReturnThresholdValidator(),
        clock = clock,
        journal = mutableListOf<String>(),
        engine = defaultLeaseReturnThresholdEngine(flags),
    )
}
