package com.acme.transport.crewing.qualification.qualificationassignment

import com.acme.transport.crewing.clock.Clock

/**
 * Moves scheduled qualification thresholds through their life cycle and keeps a journal of what it did.
 */
data class ScheduledQualificationThresholdService(
    var store: ScheduledQualificationThresholdStore,
    var validator: ScheduledQualificationThresholdValidator,
    var clock: Clock,
    var journal: MutableList<String>,
    var engine: ScheduledQualificationThresholdEngine,
) {
    private fun note(message: String) {
        journal.add("${clock.now()} $message")
    }

    fun start(code: String) {
        val item = store.find(code) ?: error("scheduled qualification threshold $code not found")
        validator.requireValid(item)
        item.moveTo(ScheduledQualificationThresholdStatus.IN_REVIEW)
        store.save(item)
        note("$code is in review")
    }

    fun settle(code: String, reason: String) {
        val item = store.find(code) ?: error("scheduled qualification threshold $code not found")
        if (item.isTerminal()) {
            note("$code already closed")
            return
        }
        item.moveTo(ScheduledQualificationThresholdStatus.ARCHIVED)
        store.save(item)
        note("$code is archived: $reason")
    }

    fun estimate(code: String): Double {
        val item = store.find(code) ?: error("scheduled qualification threshold $code not found")
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

fun newScheduledQualificationThresholdService(store: ScheduledQualificationThresholdStore, clock: Clock): ScheduledQualificationThresholdService {
    return ScheduledQualificationThresholdService(
        store = store,
        validator = defaultScheduledQualificationThresholdValidator(),
        clock = clock,
        journal = mutableListOf<String>(),
        engine = defaultScheduledQualificationThresholdEngine(),
    )
}
