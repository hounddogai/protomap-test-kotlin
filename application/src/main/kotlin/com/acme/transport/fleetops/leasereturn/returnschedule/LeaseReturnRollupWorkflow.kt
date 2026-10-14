package com.acme.transport.fleetops.leasereturn.returnschedule

import com.acme.transport.fleetops.clock.Clock
import com.acme.transport.kit.flags.Registry

/** Moves lease return rollups through their life cycle and keeps a journal of what it did. */
data class LeaseReturnRollupManager(
    var store: LeaseReturnRollupStore,
    var validator: LeaseReturnRollupValidator,
    var clock: Clock,
    var journal: MutableList<String>,
    var engine: LeaseReturnRollupEngine,
) {
    private fun note(message: String) {
        journal.add("${clock.now()} $message")
    }

    fun submit(code: String) {
        val item = store.find(code) ?: error("lease return rollup $code not found")
        validator.requireValid(item)
        item.moveTo(LeaseReturnRollupStatus.SCHEDULED)
        store.save(item)
        note("$code is scheduled")
    }

    fun close(code: String, reason: String) {
        val item = store.find(code) ?: error("lease return rollup $code not found")
        if (item.isTerminal()) {
            note("$code already closed")
            return
        }
        item.moveTo(LeaseReturnRollupStatus.CANCELLED)
        store.save(item)
        note("$code is cancelled: $reason")
    }

    fun quote(code: String): Double {
        val item = store.find(code) ?: error("lease return rollup $code not found")
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

fun newLeaseReturnRollupManager(store: LeaseReturnRollupStore, clock: Clock, flags: Registry): LeaseReturnRollupManager {
    return LeaseReturnRollupManager(
        store = store,
        validator = defaultLeaseReturnRollupValidator(),
        clock = clock,
        journal = mutableListOf<String>(),
        engine = defaultLeaseReturnRollupEngine(flags),
    )
}
