package com.acme.transport.fleetops.leasereturn.milestone

import com.acme.transport.fleetops.clock.Clock
import com.acme.transport.kit.flags.Registry

/** Moves regional lease return windows through their life cycle and keeps a journal of what it did. */
data class RegionalLeaseReturnWindowService(
    var store: RegionalLeaseReturnWindowStore,
    var validator: RegionalLeaseReturnWindowValidator,
    var clock: Clock,
    var journal: MutableList<String>,
    var engine: RegionalLeaseReturnWindowEngine,
) {
    private fun note(message: String) {
        journal.add("${clock.now()} $message")
    }

    fun submit(code: String) {
        val item = store.find(code) ?: error("regional lease return window $code not found")
        validator.requireValid(item)
        item.moveTo(RegionalLeaseReturnWindowStatus.QUEUED)
        store.save(item)
        note("$code is queued")
    }

    fun finish(code: String, reason: String) {
        val item = store.find(code) ?: error("regional lease return window $code not found")
        if (item.isTerminal()) {
            note("$code already closed")
            return
        }
        item.moveTo(RegionalLeaseReturnWindowStatus.FAILED)
        store.save(item)
        note("$code is failed: $reason")
    }

    fun evaluate(code: String): Double {
        val item = store.find(code) ?: error("regional lease return window $code not found")
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

fun newRegionalLeaseReturnWindowService(store: RegionalLeaseReturnWindowStore, clock: Clock, flags: Registry): RegionalLeaseReturnWindowService {
    return RegionalLeaseReturnWindowService(
        store = store,
        validator = defaultRegionalLeaseReturnWindowValidator(),
        clock = clock,
        journal = mutableListOf<String>(),
        engine = defaultRegionalLeaseReturnWindowEngine(flags),
    )
}
