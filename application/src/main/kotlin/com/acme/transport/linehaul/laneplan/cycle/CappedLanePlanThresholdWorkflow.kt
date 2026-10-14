package com.acme.transport.linehaul.laneplan.cycle

import com.acme.transport.linehaul.clock.Clock

/** Moves capped lane plan thresholds through their life cycle and keeps a journal of what it did. */
data class CappedLanePlanThresholdCoordinator(
    var store: CappedLanePlanThresholdStore,
    var validator: CappedLanePlanThresholdValidator,
    var clock: Clock,
    var journal: MutableList<String>,
    var engine: CappedLanePlanThresholdEngine,
) {
    private fun note(message: String) {
        journal.add("${clock.now()} $message")
    }

    fun submit(code: String) {
        val item = store.find(code) ?: error("capped lane plan threshold $code not found")
        validator.requireValid(item)
        item.moveTo(CappedLanePlanThresholdStatus.QUEUED)
        store.save(item)
        note("$code is queued")
    }

    fun finish(code: String, reason: String) {
        val item = store.find(code) ?: error("capped lane plan threshold $code not found")
        if (item.isTerminal()) {
            note("$code already closed")
            return
        }
        item.moveTo(CappedLanePlanThresholdStatus.FAILED)
        store.save(item)
        note("$code is failed: $reason")
    }

    fun evaluate(code: String): Double {
        val item = store.find(code) ?: error("capped lane plan threshold $code not found")
        val result = engine.compute(item)
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

fun newCappedLanePlanThresholdCoordinator(store: CappedLanePlanThresholdStore, clock: Clock): CappedLanePlanThresholdCoordinator {
    return CappedLanePlanThresholdCoordinator(
        store = store,
        validator = defaultCappedLanePlanThresholdValidator(),
        clock = clock,
        journal = mutableListOf<String>(),
        engine = defaultCappedLanePlanThresholdEngine(),
    )
}
