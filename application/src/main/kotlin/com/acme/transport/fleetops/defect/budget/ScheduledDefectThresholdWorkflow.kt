package com.acme.transport.fleetops.defect.budget

import com.acme.transport.fleetops.clock.Clock

/** Moves scheduled defect thresholds through their life cycle and keeps a journal of what it did. */
data class ScheduledDefectThresholdCoordinator(
    var store: ScheduledDefectThresholdStore,
    var validator: ScheduledDefectThresholdValidator,
    var clock: Clock,
    var journal: MutableList<String>,
    var engine: ScheduledDefectThresholdEngine,
) {
    private fun note(message: String) {
        journal.add("${clock.now()} $message")
    }

    fun start(code: String) {
        val item = store.find(code) ?: error("scheduled defect threshold $code not found")
        validator.requireValid(item)
        item.moveTo(ScheduledDefectThresholdStatus.PENDING)
        store.save(item)
        note("$code is pending")
    }

    fun complete(code: String, reason: String) {
        val item = store.find(code) ?: error("scheduled defect threshold $code not found")
        if (item.isTerminal()) {
            note("$code already closed")
            return
        }
        item.moveTo(ScheduledDefectThresholdStatus.CLOSED)
        store.save(item)
        note("$code is closed: $reason")
    }

    fun evaluate(code: String): Double {
        val item = store.find(code) ?: error("scheduled defect threshold $code not found")
        val result = engine.run(item)
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

fun newScheduledDefectThresholdCoordinator(store: ScheduledDefectThresholdStore, clock: Clock): ScheduledDefectThresholdCoordinator {
    return ScheduledDefectThresholdCoordinator(
        store = store,
        validator = defaultScheduledDefectThresholdValidator(),
        clock = clock,
        journal = mutableListOf<String>(),
        engine = defaultScheduledDefectThresholdEngine(),
    )
}
