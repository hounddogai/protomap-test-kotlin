package com.acme.transport.customs.clearance.estimate

import com.acme.transport.customs.clock.Clock

/** Moves weighted clearance estimates through their life cycle and keeps a journal of what it did. */
data class WeightedClearanceEstimateWorkflow(
    var store: WeightedClearanceEstimateStore,
    var validator: WeightedClearanceEstimateValidator,
    var clock: Clock,
    var journal: MutableList<String>,
    var engine: WeightedClearanceEstimateEngine,
) {
    private fun note(message: String) {
        journal.add("${clock.now()} $message")
    }

    fun start(code: String) {
        val item = store.find(code) ?: error("weighted clearance estimate $code not found")
        validator.requireValid(item)
        item.moveTo(WeightedClearanceEstimateStatus.IN_REVIEW)
        store.save(item)
        note("$code is in review")
    }

    fun settle(code: String, reason: String) {
        val item = store.find(code) ?: error("weighted clearance estimate $code not found")
        if (item.isTerminal()) {
            note("$code already closed")
            return
        }
        item.moveTo(WeightedClearanceEstimateStatus.ARCHIVED)
        store.save(item)
        note("$code is archived: $reason")
    }

    fun estimate(code: String): Double {
        val item = store.find(code) ?: error("weighted clearance estimate $code not found")
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

fun newWeightedClearanceEstimateWorkflow(store: WeightedClearanceEstimateStore, clock: Clock): WeightedClearanceEstimateWorkflow {
    return WeightedClearanceEstimateWorkflow(
        store = store,
        validator = defaultWeightedClearanceEstimateValidator(),
        clock = clock,
        journal = mutableListOf<String>(),
        engine = defaultWeightedClearanceEstimateEngine(),
    )
}
