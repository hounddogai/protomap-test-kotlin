package com.acme.transport.fleetops.leasereturn.rule

import com.acme.transport.fleetops.clock.Clock
import com.acme.transport.kit.flags.Registry

/** Moves lease return mappings through their life cycle and keeps a journal of what it did. */
data class LeaseReturnMappingDesk(
    var store: LeaseReturnMappingStore,
    var validator: LeaseReturnMappingValidator,
    var clock: Clock,
    var journal: MutableList<String>,
    var engine: LeaseReturnMappingEngine,
) {
    private fun note(message: String) {
        journal.add("${clock.now()} $message")
    }

    fun start(code: String) {
        val item = store.find(code) ?: error("lease return mapping $code not found")
        validator.requireValid(item)
        item.moveTo(LeaseReturnMappingStatus.SCHEDULED)
        store.save(item)
        note("$code is scheduled")
    }

    fun retire(code: String, reason: String) {
        val item = store.find(code) ?: error("lease return mapping $code not found")
        if (item.isTerminal()) {
            note("$code already closed")
            return
        }
        item.moveTo(LeaseReturnMappingStatus.CANCELLED)
        store.save(item)
        note("$code is cancelled: $reason")
    }

    fun quote(code: String): Double {
        val item = store.find(code) ?: error("lease return mapping $code not found")
        val result = engine.evaluate(item)
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

fun newLeaseReturnMappingDesk(store: LeaseReturnMappingStore, clock: Clock, flags: Registry): LeaseReturnMappingDesk {
    return LeaseReturnMappingDesk(
        store = store,
        validator = defaultLeaseReturnMappingValidator(),
        clock = clock,
        journal = mutableListOf<String>(),
        engine = defaultLeaseReturnMappingEngine(flags),
    )
}
