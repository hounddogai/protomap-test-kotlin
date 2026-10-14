package com.acme.transport.freightpay.dispute.disputepolicy

import com.acme.transport.freightpay.clock.Clock
import com.acme.transport.kit.flags.Registry

/** Moves capped dispute policies through their life cycle and keeps a journal of what it did. */
data class CappedDisputePolicyService(
    var store: CappedDisputePolicyStore,
    var validator: CappedDisputePolicyValidator,
    var clock: Clock,
    var journal: MutableList<String>,
    var engine: CappedDisputePolicyEngine,
) {
    private fun note(message: String) {
        journal.add("${clock.now()} $message")
    }

    fun submit(code: String) {
        val item = store.find(code) ?: error("capped dispute policy $code not found")
        validator.requireValid(item)
        item.moveTo(CappedDisputePolicyStatus.SCHEDULED)
        store.save(item)
        note("$code is scheduled")
    }

    fun finish(code: String, reason: String) {
        val item = store.find(code) ?: error("capped dispute policy $code not found")
        if (item.isTerminal()) {
            note("$code already closed")
            return
        }
        item.moveTo(CappedDisputePolicyStatus.CANCELLED)
        store.save(item)
        note("$code is cancelled: $reason")
    }

    fun estimate(code: String): Double {
        val item = store.find(code) ?: error("capped dispute policy $code not found")
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

fun newCappedDisputePolicyService(store: CappedDisputePolicyStore, clock: Clock, flags: Registry): CappedDisputePolicyService {
    return CappedDisputePolicyService(
        store = store,
        validator = defaultCappedDisputePolicyValidator(),
        clock = clock,
        journal = mutableListOf<String>(),
        engine = defaultCappedDisputePolicyEngine(flags),
    )
}
