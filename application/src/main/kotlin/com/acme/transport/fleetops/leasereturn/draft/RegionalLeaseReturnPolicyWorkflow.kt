package com.acme.transport.fleetops.leasereturn.draft

import com.acme.transport.fleetops.clock.Clock
import com.acme.transport.kit.flags.Registry

/**
 * Moves regional lease return policies through their life cycle and keeps a journal of what it did.
 */
data class RegionalLeaseReturnPolicyService(
    var store: RegionalLeaseReturnPolicyStore,
    var validator: RegionalLeaseReturnPolicyValidator,
    var clock: Clock,
    var journal: MutableList<String>,
    var engine: RegionalLeaseReturnPolicyEngine,
) {
    private fun note(message: String) {
        journal.add("${clock.now()} $message")
    }

    fun advance(code: String) {
        val item = store.find(code) ?: error("regional lease return policy $code not found")
        validator.requireValid(item)
        item.moveTo(RegionalLeaseReturnPolicyStatus.QUEUED)
        store.save(item)
        note("$code is queued")
    }

    fun finish(code: String, reason: String) {
        val item = store.find(code) ?: error("regional lease return policy $code not found")
        if (item.isTerminal()) {
            note("$code already closed")
            return
        }
        item.moveTo(RegionalLeaseReturnPolicyStatus.FAILED)
        store.save(item)
        note("$code is failed: $reason")
    }

    fun estimate(code: String): Double {
        val item = store.find(code) ?: error("regional lease return policy $code not found")
        val result = engine.compute(item)
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

fun newRegionalLeaseReturnPolicyService(store: RegionalLeaseReturnPolicyStore, clock: Clock, flags: Registry): RegionalLeaseReturnPolicyService {
    return RegionalLeaseReturnPolicyService(
        store = store,
        validator = defaultRegionalLeaseReturnPolicyValidator(),
        clock = clock,
        journal = mutableListOf<String>(),
        engine = defaultRegionalLeaseReturnPolicyEngine(flags),
    )
}
