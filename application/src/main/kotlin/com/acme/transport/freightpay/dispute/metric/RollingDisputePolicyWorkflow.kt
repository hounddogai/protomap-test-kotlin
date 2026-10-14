package com.acme.transport.freightpay.dispute.metric

import com.acme.transport.freightpay.clock.Clock
import com.acme.transport.kit.flags.Registry

/** Moves rolling dispute policies through their life cycle and keeps a journal of what it did. */
data class RollingDisputePolicyCoordinator(
    var store: RollingDisputePolicyStore,
    var validator: RollingDisputePolicyValidator,
    var clock: Clock,
    var journal: MutableList<String>,
    var engine: RollingDisputePolicyEngine,
) {
    private fun note(message: String) {
        journal.add("${clock.now()} $message")
    }

    fun start(code: String) {
        val item = store.find(code) ?: error("rolling dispute policy $code not found")
        validator.requireValid(item)
        item.moveTo(RollingDisputePolicyStatus.VALIDATED)
        store.save(item)
        note("$code is validated")
    }

    fun complete(code: String, reason: String) {
        val item = store.find(code) ?: error("rolling dispute policy $code not found")
        if (item.isTerminal()) {
            note("$code already closed")
            return
        }
        item.moveTo(RollingDisputePolicyStatus.RETIRED)
        store.save(item)
        note("$code is retired: $reason")
    }

    fun quote(code: String): Double {
        val item = store.find(code) ?: error("rolling dispute policy $code not found")
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
            if (item.updatedAt > cutoff) {
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

fun newRollingDisputePolicyCoordinator(store: RollingDisputePolicyStore, clock: Clock, flags: Registry): RollingDisputePolicyCoordinator {
    return RollingDisputePolicyCoordinator(
        store = store,
        validator = defaultRollingDisputePolicyValidator(),
        clock = clock,
        journal = mutableListOf<String>(),
        engine = defaultRollingDisputePolicyEngine(flags),
    )
}
