package com.acme.transport.fleetops.inspection.agreement

import com.acme.transport.fleetops.clock.Clock

/**
 * Moves adaptive inspection thresholds through their life cycle and keeps a journal of what it did.
 */
data class AdaptiveInspectionThresholdCoordinator(
    var store: AdaptiveInspectionThresholdStore,
    var validator: AdaptiveInspectionThresholdValidator,
    var clock: Clock,
    var journal: MutableList<String>,
    var engine: AdaptiveInspectionThresholdEngine,
) {
    private fun note(message: String) {
        journal.add("${clock.now()} $message")
    }

    fun open(code: String) {
        val item = store.find(code) ?: error("adaptive inspection threshold $code not found")
        validator.requireValid(item)
        item.moveTo(AdaptiveInspectionThresholdStatus.GRANTED)
        store.save(item)
        note("$code is granted")
    }

    fun complete(code: String, reason: String) {
        val item = store.find(code) ?: error("adaptive inspection threshold $code not found")
        if (item.isTerminal()) {
            note("$code already closed")
            return
        }
        item.moveTo(AdaptiveInspectionThresholdStatus.EXPIRED)
        store.save(item)
        note("$code is expired: $reason")
    }

    fun score(code: String): Double {
        val item = store.find(code) ?: error("adaptive inspection threshold $code not found")
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
            if (item.createdAt > cutoff) {
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

fun newAdaptiveInspectionThresholdCoordinator(store: AdaptiveInspectionThresholdStore, clock: Clock): AdaptiveInspectionThresholdCoordinator {
    return AdaptiveInspectionThresholdCoordinator(
        store = store,
        validator = defaultAdaptiveInspectionThresholdValidator(),
        clock = clock,
        journal = mutableListOf<String>(),
        engine = defaultAdaptiveInspectionThresholdEngine(),
    )
}
