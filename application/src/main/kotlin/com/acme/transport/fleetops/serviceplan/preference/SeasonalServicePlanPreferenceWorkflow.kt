package com.acme.transport.fleetops.serviceplan.preference

import com.acme.transport.fleetops.clock.Clock
import com.acme.transport.kit.flags.Registry

/**
 * Moves seasonal service plan preferences through their life cycle and keeps a journal of what it did.
 */
data class SeasonalServicePlanPreferenceWorkflow(
    var store: SeasonalServicePlanPreferenceStore,
    var validator: SeasonalServicePlanPreferenceValidator,
    var clock: Clock,
    var journal: MutableList<String>,
    var engine: SeasonalServicePlanPreferenceEngine,
) {
    private fun note(message: String) {
        journal.add("${clock.now()} $message")
    }

    fun open(code: String) {
        val item = store.find(code) ?: error("seasonal service plan preference $code not found")
        validator.requireValid(item)
        item.moveTo(SeasonalServicePlanPreferenceStatus.APPROVED)
        store.save(item)
        note("$code is approved")
    }

    fun settle(code: String, reason: String) {
        val item = store.find(code) ?: error("seasonal service plan preference $code not found")
        if (item.isTerminal()) {
            note("$code already closed")
            return
        }
        item.moveTo(SeasonalServicePlanPreferenceStatus.WITHDRAWN)
        store.save(item)
        note("$code is withdrawn: $reason")
    }

    fun score(code: String): Double {
        val item = store.find(code) ?: error("seasonal service plan preference $code not found")
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

fun newSeasonalServicePlanPreferenceWorkflow(store: SeasonalServicePlanPreferenceStore, clock: Clock, flags: Registry): SeasonalServicePlanPreferenceWorkflow {
    return SeasonalServicePlanPreferenceWorkflow(
        store = store,
        validator = defaultSeasonalServicePlanPreferenceValidator(),
        clock = clock,
        journal = mutableListOf<String>(),
        engine = defaultSeasonalServicePlanPreferenceEngine(flags),
    )
}
