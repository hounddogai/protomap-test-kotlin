package com.acme.transport.fleetops.inspection.draft

import com.acme.transport.fleetops.clock.Clock
import com.acme.transport.kit.flags.Registry

/** Moves pending inspection schedules through their life cycle and keeps a journal of what it did. */
data class PendingInspectionScheduleCoordinator(
    var store: PendingInspectionScheduleStore,
    var validator: PendingInspectionScheduleValidator,
    var clock: Clock,
    var journal: MutableList<String>,
    var engine: PendingInspectionScheduleEngine,
) {
    private fun note(message: String) {
        journal.add("${clock.now()} $message")
    }

    fun advance(code: String) {
        val item = store.find(code) ?: error("pending inspection schedule $code not found")
        validator.requireValid(item)
        item.moveTo(PendingInspectionScheduleStatus.VALIDATED)
        store.save(item)
        note("$code is validated")
    }

    fun complete(code: String, reason: String) {
        val item = store.find(code) ?: error("pending inspection schedule $code not found")
        if (item.isTerminal()) {
            note("$code already closed")
            return
        }
        item.moveTo(PendingInspectionScheduleStatus.RETIRED)
        store.save(item)
        note("$code is retired: $reason")
    }

    fun estimate(code: String): Double {
        val item = store.find(code) ?: error("pending inspection schedule $code not found")
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

fun newPendingInspectionScheduleCoordinator(store: PendingInspectionScheduleStore, clock: Clock, flags: Registry): PendingInspectionScheduleCoordinator {
    return PendingInspectionScheduleCoordinator(
        store = store,
        validator = defaultPendingInspectionScheduleValidator(),
        clock = clock,
        journal = mutableListOf<String>(),
        engine = defaultPendingInspectionScheduleEngine(flags),
    )
}
