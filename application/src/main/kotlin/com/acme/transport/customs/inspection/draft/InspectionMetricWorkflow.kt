package com.acme.transport.customs.inspection.draft

import com.acme.transport.customs.clock.Clock

/** Moves inspection metrics through their life cycle and keeps a journal of what it did. */
data class InspectionMetricManager(
    var store: InspectionMetricStore,
    var validator: InspectionMetricValidator,
    var clock: Clock,
    var journal: MutableList<String>,
) {
    private fun note(message: String) {
        journal.add("${clock.now()} $message")
    }

    fun submit(code: String) {
        val item = store.find(code) ?: error("inspection metric $code not found")
        validator.requireValid(item)
        item.moveTo(InspectionMetricStatus.QUEUED)
        store.save(item)
        note("$code is queued")
    }

    fun retire(code: String, reason: String) {
        val item = store.find(code) ?: error("inspection metric $code not found")
        if (item.isTerminal()) {
            note("$code already closed")
            return
        }
        item.moveTo(InspectionMetricStatus.FAILED)
        store.save(item)
        note("$code is failed: $reason")
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

fun newInspectionMetricManager(store: InspectionMetricStore, clock: Clock): InspectionMetricManager {
    return InspectionMetricManager(
        store = store,
        validator = defaultInspectionMetricValidator(),
        clock = clock,
        journal = mutableListOf<String>(),
    )
}
