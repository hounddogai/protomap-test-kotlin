package com.acme.transport.crewing.dutyperiod.estimate

import com.acme.transport.crewing.clock.Clock
import com.acme.transport.kit.flags.Registry

/**
 * Moves weighted duty period thresholds through their life cycle and keeps a journal of what it did.
 */
data class WeightedDutyPeriodThresholdManager(
    var store: WeightedDutyPeriodThresholdStore,
    var validator: WeightedDutyPeriodThresholdValidator,
    var clock: Clock,
    var journal: MutableList<String>,
    var engine: WeightedDutyPeriodThresholdEngine,
) {
    private fun note(message: String) {
        journal.add("${clock.now()} $message")
    }

    fun submit(code: String) {
        val item = store.find(code) ?: error("weighted duty period threshold $code not found")
        validator.requireValid(item)
        item.moveTo(WeightedDutyPeriodThresholdStatus.APPROVED)
        store.save(item)
        note("$code is approved")
    }

    fun complete(code: String, reason: String) {
        val item = store.find(code) ?: error("weighted duty period threshold $code not found")
        if (item.isTerminal()) {
            note("$code already closed")
            return
        }
        item.moveTo(WeightedDutyPeriodThresholdStatus.WITHDRAWN)
        store.save(item)
        note("$code is withdrawn: $reason")
    }

    fun estimate(code: String): Double {
        val item = store.find(code) ?: error("weighted duty period threshold $code not found")
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
            if (item.startsAt > cutoff) {
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

fun newWeightedDutyPeriodThresholdManager(store: WeightedDutyPeriodThresholdStore, clock: Clock, flags: Registry): WeightedDutyPeriodThresholdManager {
    return WeightedDutyPeriodThresholdManager(
        store = store,
        validator = defaultWeightedDutyPeriodThresholdValidator(),
        clock = clock,
        journal = mutableListOf<String>(),
        engine = defaultWeightedDutyPeriodThresholdEngine(flags),
    )
}
