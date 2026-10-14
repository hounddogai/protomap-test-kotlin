package com.acme.transport.fleetops.leasereturn.returnforecast

import com.acme.transport.fleetops.clock.Clock
import com.acme.transport.kit.flags.Registry

/** Moves adaptive lease return tiers through their life cycle and keeps a journal of what it did. */
data class AdaptiveLeaseReturnTierWorkflow(
    var store: AdaptiveLeaseReturnTierStore,
    var validator: AdaptiveLeaseReturnTierValidator,
    var clock: Clock,
    var journal: MutableList<String>,
    var engine: AdaptiveLeaseReturnTierEngine,
) {
    private fun note(message: String) {
        journal.add("${clock.now()} $message")
    }

    fun start(code: String) {
        val item = store.find(code) ?: error("adaptive lease return tier $code not found")
        validator.requireValid(item)
        item.moveTo(AdaptiveLeaseReturnTierStatus.QUEUED)
        store.save(item)
        note("$code is queued")
    }

    fun complete(code: String, reason: String) {
        val item = store.find(code) ?: error("adaptive lease return tier $code not found")
        if (item.isTerminal()) {
            note("$code already closed")
            return
        }
        item.moveTo(AdaptiveLeaseReturnTierStatus.FAILED)
        store.save(item)
        note("$code is failed: $reason")
    }

    fun quote(code: String): Double {
        val item = store.find(code) ?: error("adaptive lease return tier $code not found")
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

fun newAdaptiveLeaseReturnTierWorkflow(store: AdaptiveLeaseReturnTierStore, clock: Clock, flags: Registry): AdaptiveLeaseReturnTierWorkflow {
    return AdaptiveLeaseReturnTierWorkflow(
        store = store,
        validator = defaultAdaptiveLeaseReturnTierValidator(),
        clock = clock,
        journal = mutableListOf<String>(),
        engine = defaultAdaptiveLeaseReturnTierEngine(flags),
    )
}
