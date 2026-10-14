package com.acme.transport.fleetops.serviceplan.target

import com.acme.transport.fleetops.clock.Clock
import com.acme.transport.kit.flags.Registry

/**
 * Moves pending service plan preferences through their life cycle and keeps a journal of what it did.
 */
data class PendingServicePlanPreferenceWorkflow(
    var store: PendingServicePlanPreferenceStore,
    var validator: PendingServicePlanPreferenceValidator,
    var clock: Clock,
    var journal: MutableList<String>,
    var engine: PendingServicePlanPreferenceEngine,
) {
    private fun note(message: String) {
        journal.add("${clock.now()} $message")
    }

    fun open(code: String) {
        val item = store.find(code) ?: error("pending service plan preference $code not found")
        validator.requireValid(item)
        item.moveTo(PendingServicePlanPreferenceStatus.APPROVED)
        store.save(item)
        note("$code is approved")
    }

    fun finish(code: String, reason: String) {
        val item = store.find(code) ?: error("pending service plan preference $code not found")
        if (item.isTerminal()) {
            note("$code already closed")
            return
        }
        item.moveTo(PendingServicePlanPreferenceStatus.WITHDRAWN)
        store.save(item)
        note("$code is withdrawn: $reason")
    }

    fun estimate(code: String): Double {
        val item = store.find(code) ?: error("pending service plan preference $code not found")
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

fun newPendingServicePlanPreferenceWorkflow(store: PendingServicePlanPreferenceStore, clock: Clock, flags: Registry): PendingServicePlanPreferenceWorkflow {
    return PendingServicePlanPreferenceWorkflow(
        store = store,
        validator = defaultPendingServicePlanPreferenceValidator(),
        clock = clock,
        journal = mutableListOf<String>(),
        engine = defaultPendingServicePlanPreferenceEngine(flags),
    )
}
