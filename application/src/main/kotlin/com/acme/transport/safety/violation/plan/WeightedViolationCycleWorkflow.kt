package com.acme.transport.safety.violation.plan

import com.acme.transport.safety.clock.Clock

/** Moves weighted violation cycles through their life cycle and keeps a journal of what it did. */
data class WeightedViolationCycleCoordinator(
    var store: WeightedViolationCycleStore,
    var validator: WeightedViolationCycleValidator,
    var clock: Clock,
    var journal: MutableList<String>,
    var engine: WeightedViolationCycleEngine,
) {
    private fun note(message: String) {
        journal.add("${clock.now()} $message")
    }

    fun open(code: String) {
        val item = store.find(code) ?: error("weighted violation cycle $code not found")
        validator.requireValid(item)
        item.moveTo(WeightedViolationCycleStatus.QUEUED)
        store.save(item)
        note("$code is queued")
    }

    fun finish(code: String, reason: String) {
        val item = store.find(code) ?: error("weighted violation cycle $code not found")
        if (item.isTerminal()) {
            note("$code already closed")
            return
        }
        item.moveTo(WeightedViolationCycleStatus.FAILED)
        store.save(item)
        note("$code is failed: $reason")
    }

    fun quote(code: String): Double {
        val item = store.find(code) ?: error("weighted violation cycle $code not found")
        val result = engine.evaluate(item)
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

fun newWeightedViolationCycleCoordinator(store: WeightedViolationCycleStore, clock: Clock): WeightedViolationCycleCoordinator {
    return WeightedViolationCycleCoordinator(
        store = store,
        validator = defaultWeightedViolationCycleValidator(),
        clock = clock,
        journal = mutableListOf<String>(),
        engine = defaultWeightedViolationCycleEngine(),
    )
}
