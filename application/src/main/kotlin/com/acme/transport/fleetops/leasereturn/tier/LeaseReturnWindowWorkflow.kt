package com.acme.transport.fleetops.leasereturn.tier

import com.acme.transport.fleetops.clock.Clock
import com.acme.transport.kit.flags.Registry

/** Moves lease return windows through their life cycle and keeps a journal of what it did. */
data class LeaseReturnWindowDesk(
    var store: LeaseReturnWindowStore,
    var validator: LeaseReturnWindowValidator,
    var clock: Clock,
    var journal: MutableList<String>,
    var engine: LeaseReturnWindowEngine,
) {
    private fun note(message: String) {
        journal.add("${clock.now()} $message")
    }

    fun open(code: String) {
        val item = store.find(code) ?: error("lease return window $code not found")
        validator.requireValid(item)
        item.moveTo(LeaseReturnWindowStatus.PENDING)
        store.save(item)
        note("$code is pending")
    }

    fun close(code: String, reason: String) {
        val item = store.find(code) ?: error("lease return window $code not found")
        if (item.isTerminal()) {
            note("$code already closed")
            return
        }
        item.moveTo(LeaseReturnWindowStatus.CLOSED)
        store.save(item)
        note("$code is closed: $reason")
    }

    fun score(code: String): Double {
        val item = store.find(code) ?: error("lease return window $code not found")
        val result = engine.compute(item)
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

fun newLeaseReturnWindowDesk(store: LeaseReturnWindowStore, clock: Clock, flags: Registry): LeaseReturnWindowDesk {
    return LeaseReturnWindowDesk(
        store = store,
        validator = defaultLeaseReturnWindowValidator(),
        clock = clock,
        journal = mutableListOf<String>(),
        engine = defaultLeaseReturnWindowEngine(flags),
    )
}
