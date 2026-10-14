package com.acme.transport.crewing.qualification.qualificationmetric

import com.acme.transport.crewing.clock.Clock

/**
 * Moves manual qualification thresholds through their life cycle and keeps a journal of what it did.
 */
data class ManualQualificationThresholdCoordinator(
    var store: ManualQualificationThresholdStore,
    var validator: ManualQualificationThresholdValidator,
    var clock: Clock,
    var journal: MutableList<String>,
    var engine: ManualQualificationThresholdEngine,
) {
    private fun note(message: String) {
        journal.add("${clock.now()} $message")
    }

    fun submit(code: String) {
        val item = store.find(code) ?: error("manual qualification threshold $code not found")
        validator.requireValid(item)
        item.moveTo(ManualQualificationThresholdStatus.WARMING)
        store.save(item)
        note("$code is warming")
    }

    fun retire(code: String, reason: String) {
        val item = store.find(code) ?: error("manual qualification threshold $code not found")
        if (item.isTerminal()) {
            note("$code already closed")
            return
        }
        item.moveTo(ManualQualificationThresholdStatus.STOPPED)
        store.save(item)
        note("$code is stopped: $reason")
    }

    fun score(code: String): Double {
        val item = store.find(code) ?: error("manual qualification threshold $code not found")
        val result = engine.price(item)
        note("$code evaluated at $result")
        return result
    }

    fun sweep(cutoff: Int): Int {
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

fun newManualQualificationThresholdCoordinator(store: ManualQualificationThresholdStore, clock: Clock): ManualQualificationThresholdCoordinator {
    return ManualQualificationThresholdCoordinator(
        store = store,
        validator = defaultManualQualificationThresholdValidator(),
        clock = clock,
        journal = mutableListOf<String>(),
        engine = defaultManualQualificationThresholdEngine(),
    )
}
