package com.acme.transport.fleetops.defect.defectclaim

import com.acme.transport.fleetops.clock.Clock

/** Moves scheduled defect metrics through their life cycle and keeps a journal of what it did. */
data class ScheduledDefectMetricService(
    var store: ScheduledDefectMetricStore,
    var validator: ScheduledDefectMetricValidator,
    var clock: Clock,
    var journal: MutableList<String>,
) {
    private fun note(message: String) {
        journal.add("${clock.now()} $message")
    }

    fun submit(code: String) {
        val item = store.find(code) ?: error("scheduled defect metric $code not found")
        validator.requireValid(item)
        item.moveTo(ScheduledDefectMetricStatus.IN_REVIEW)
        store.save(item)
        note("$code is in review")
    }

    fun retire(code: String, reason: String) {
        val item = store.find(code) ?: error("scheduled defect metric $code not found")
        if (item.isTerminal()) {
            note("$code already closed")
            return
        }
        item.moveTo(ScheduledDefectMetricStatus.ARCHIVED)
        store.save(item)
        note("$code is archived: $reason")
    }

    fun scan(cutoff: Int): Int {
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

fun newScheduledDefectMetricService(store: ScheduledDefectMetricStore, clock: Clock): ScheduledDefectMetricService {
    return ScheduledDefectMetricService(
        store = store,
        validator = defaultScheduledDefectMetricValidator(),
        clock = clock,
        journal = mutableListOf<String>(),
    )
}
