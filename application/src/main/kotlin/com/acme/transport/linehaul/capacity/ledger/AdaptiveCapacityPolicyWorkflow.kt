package com.acme.transport.linehaul.capacity.ledger

import com.acme.transport.kit.flags.Registry
import com.acme.transport.linehaul.clock.Clock

/** Moves adaptive capacity policies through their life cycle and keeps a journal of what it did. */
data class AdaptiveCapacityPolicyService(
    var store: AdaptiveCapacityPolicyStore,
    var validator: AdaptiveCapacityPolicyValidator,
    var clock: Clock,
    var journal: MutableList<String>,
    var engine: AdaptiveCapacityPolicyEngine,
) {
    private fun note(message: String) {
        journal.add("${clock.now()} $message")
    }

    fun open(code: String) {
        val item = store.find(code) ?: error("adaptive capacity policy $code not found")
        validator.requireValid(item)
        item.moveTo(AdaptiveCapacityPolicyStatus.PENDING)
        store.save(item)
        note("$code is pending")
    }

    fun complete(code: String, reason: String) {
        val item = store.find(code) ?: error("adaptive capacity policy $code not found")
        if (item.isTerminal()) {
            note("$code already closed")
            return
        }
        item.moveTo(AdaptiveCapacityPolicyStatus.CLOSED)
        store.save(item)
        note("$code is closed: $reason")
    }

    fun estimate(code: String): Double {
        val item = store.find(code) ?: error("adaptive capacity policy $code not found")
        val result = engine.run(item)
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

fun newAdaptiveCapacityPolicyService(store: AdaptiveCapacityPolicyStore, clock: Clock, flags: Registry): AdaptiveCapacityPolicyService {
    return AdaptiveCapacityPolicyService(
        store = store,
        validator = defaultAdaptiveCapacityPolicyValidator(),
        clock = clock,
        journal = mutableListOf<String>(),
        engine = defaultAdaptiveCapacityPolicyEngine(flags),
    )
}
