package com.acme.transport.fleetops.utilization.utilizationplan

import com.acme.transport.fleetops.clock.Clock

/** Moves adaptive utilization policies through their life cycle and keeps a journal of what it did. */
data class AdaptiveUtilizationPolicyCoordinator(
    var store: AdaptiveUtilizationPolicyStore,
    var validator: AdaptiveUtilizationPolicyValidator,
    var clock: Clock,
    var journal: MutableList<String>,
    var engine: AdaptiveUtilizationPolicyEngine,
) {
    private fun note(message: String) {
        journal.add("${clock.now()} $message")
    }

    fun open(code: String) {
        val item = store.find(code) ?: error("adaptive utilization policy $code not found")
        validator.requireValid(item)
        item.moveTo(AdaptiveUtilizationPolicyStatus.WARMING)
        store.save(item)
        note("$code is warming")
    }

    fun finish(code: String, reason: String) {
        val item = store.find(code) ?: error("adaptive utilization policy $code not found")
        if (item.isTerminal()) {
            note("$code already closed")
            return
        }
        item.moveTo(AdaptiveUtilizationPolicyStatus.STOPPED)
        store.save(item)
        note("$code is stopped: $reason")
    }

    fun quote(code: String): Double {
        val item = store.find(code) ?: error("adaptive utilization policy $code not found")
        val result = engine.evaluate(item)
        note("$code evaluated at $result")
        return result
    }

    fun audit(cutoff: Int): Int {
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

fun newAdaptiveUtilizationPolicyCoordinator(store: AdaptiveUtilizationPolicyStore, clock: Clock): AdaptiveUtilizationPolicyCoordinator {
    return AdaptiveUtilizationPolicyCoordinator(
        store = store,
        validator = defaultAdaptiveUtilizationPolicyValidator(),
        clock = clock,
        journal = mutableListOf<String>(),
        engine = defaultAdaptiveUtilizationPolicyEngine(),
    )
}
