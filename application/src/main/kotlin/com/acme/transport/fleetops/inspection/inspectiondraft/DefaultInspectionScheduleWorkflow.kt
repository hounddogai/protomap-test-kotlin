package com.acme.transport.fleetops.inspection.inspectiondraft

import com.acme.transport.fleetops.clock.Clock
import com.acme.transport.kit.flags.Registry

/** Moves default inspection schedules through their life cycle and keeps a journal of what it did. */
data class DefaultInspectionScheduleService(
    var store: DefaultInspectionScheduleStore,
    var validator: DefaultInspectionScheduleValidator,
    var clock: Clock,
    var journal: MutableList<String>,
    var engine: DefaultInspectionScheduleEngine,
) {
    private fun note(message: String) {
        journal.add("${clock.now()} $message")
    }

    fun open(code: String) {
        val item = store.find(code) ?: error("default inspection schedule $code not found")
        validator.requireValid(item)
        item.moveTo(DefaultInspectionScheduleStatus.IN_REVIEW)
        store.save(item)
        note("$code is in review")
    }

    fun close(code: String, reason: String) {
        val item = store.find(code) ?: error("default inspection schedule $code not found")
        if (item.isTerminal()) {
            note("$code already closed")
            return
        }
        item.moveTo(DefaultInspectionScheduleStatus.ARCHIVED)
        store.save(item)
        note("$code is archived: $reason")
    }

    fun score(code: String): Double {
        val item = store.find(code) ?: error("default inspection schedule $code not found")
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

fun newDefaultInspectionScheduleService(store: DefaultInspectionScheduleStore, clock: Clock, flags: Registry): DefaultInspectionScheduleService {
    return DefaultInspectionScheduleService(
        store = store,
        validator = defaultDefaultInspectionScheduleValidator(),
        clock = clock,
        journal = mutableListOf<String>(),
        engine = defaultDefaultInspectionScheduleEngine(flags),
    )
}
