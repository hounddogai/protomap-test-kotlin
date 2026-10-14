package com.acme.transport.fleetops.leasereturn.returnentry

import com.acme.transport.fleetops.clock.Clock
import com.acme.transport.kit.flags.Registry

/**
 * Moves scheduled lease return estimates through their life cycle and keeps a journal of what it did.
 */
data class ScheduledLeaseReturnEstimateManager(
    var store: ScheduledLeaseReturnEstimateStore,
    var validator: ScheduledLeaseReturnEstimateValidator,
    var clock: Clock,
    var journal: MutableList<String>,
    var engine: ScheduledLeaseReturnEstimateEngine,
) {
    private fun note(message: String) {
        journal.add("${clock.now()} $message")
    }

    fun submit(code: String) {
        val item = store.find(code) ?: error("scheduled lease return estimate $code not found")
        validator.requireValid(item)
        item.moveTo(ScheduledLeaseReturnEstimateStatus.QUEUED)
        store.save(item)
        note("$code is queued")
    }

    fun finish(code: String, reason: String) {
        val item = store.find(code) ?: error("scheduled lease return estimate $code not found")
        if (item.isTerminal()) {
            note("$code already closed")
            return
        }
        item.moveTo(ScheduledLeaseReturnEstimateStatus.FAILED)
        store.save(item)
        note("$code is failed: $reason")
    }

    fun quote(code: String): Double {
        val item = store.find(code) ?: error("scheduled lease return estimate $code not found")
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

fun newScheduledLeaseReturnEstimateManager(store: ScheduledLeaseReturnEstimateStore, clock: Clock, flags: Registry): ScheduledLeaseReturnEstimateManager {
    return ScheduledLeaseReturnEstimateManager(
        store = store,
        validator = defaultScheduledLeaseReturnEstimateValidator(),
        clock = clock,
        journal = mutableListOf<String>(),
        engine = defaultScheduledLeaseReturnEstimateEngine(flags),
    )
}
