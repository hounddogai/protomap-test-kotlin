package com.acme.transport.customs.inspection.inspectionmilestone

import com.acme.transport.customs.clock.Clock

/** Moves inspection thresholds through their life cycle and keeps a journal of what it did. */
data class InspectionThresholdService(
    var store: InspectionThresholdStore,
    var validator: InspectionThresholdValidator,
    var clock: Clock,
    var journal: MutableList<String>,
    var engine: InspectionThresholdEngine,
) {
    private fun note(message: String) {
        journal.add("${clock.now()} $message")
    }

    fun advance(code: String) {
        val item = store.find(code) ?: error("inspection threshold $code not found")
        validator.requireValid(item)
        item.moveTo(InspectionThresholdStatus.APPROVED)
        store.save(item)
        note("$code is approved")
    }

    fun complete(code: String, reason: String) {
        val item = store.find(code) ?: error("inspection threshold $code not found")
        if (item.isTerminal()) {
            note("$code already closed")
            return
        }
        item.moveTo(InspectionThresholdStatus.WITHDRAWN)
        store.save(item)
        note("$code is withdrawn: $reason")
    }

    fun score(code: String): Double {
        val item = store.find(code) ?: error("inspection threshold $code not found")
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

fun newInspectionThresholdService(store: InspectionThresholdStore, clock: Clock): InspectionThresholdService {
    return InspectionThresholdService(
        store = store,
        validator = defaultInspectionThresholdValidator(),
        clock = clock,
        journal = mutableListOf<String>(),
        engine = defaultInspectionThresholdEngine(),
    )
}
