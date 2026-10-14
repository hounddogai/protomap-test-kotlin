package com.acme.transport.fleetops.serviceplan.planthreshold

import com.acme.transport.fleetops.clock.Clock

/**
 * Moves seasonal service plan thresholds through their life cycle and keeps a journal of what it did.
 */
data class SeasonalServicePlanThresholdManager(
    var store: SeasonalServicePlanThresholdStore,
    var validator: SeasonalServicePlanThresholdValidator,
    var clock: Clock,
    var journal: MutableList<String>,
    var engine: SeasonalServicePlanThresholdEngine,
) {
    private fun note(message: String) {
        journal.add("${clock.now()} $message")
    }

    fun advance(code: String) {
        val item = store.find(code) ?: error("seasonal service plan threshold $code not found")
        validator.requireValid(item)
        item.moveTo(SeasonalServicePlanThresholdStatus.QUEUED)
        store.save(item)
        note("$code is queued")
    }

    fun complete(code: String, reason: String) {
        val item = store.find(code) ?: error("seasonal service plan threshold $code not found")
        if (item.isTerminal()) {
            note("$code already closed")
            return
        }
        item.moveTo(SeasonalServicePlanThresholdStatus.FAILED)
        store.save(item)
        note("$code is failed: $reason")
    }

    fun score(code: String): Double {
        val item = store.find(code) ?: error("seasonal service plan threshold $code not found")
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

fun newSeasonalServicePlanThresholdManager(store: SeasonalServicePlanThresholdStore, clock: Clock): SeasonalServicePlanThresholdManager {
    return SeasonalServicePlanThresholdManager(
        store = store,
        validator = defaultSeasonalServicePlanThresholdValidator(),
        clock = clock,
        journal = mutableListOf<String>(),
        engine = defaultSeasonalServicePlanThresholdEngine(),
    )
}
