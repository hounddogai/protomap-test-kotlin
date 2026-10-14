package com.acme.transport.crewing.dutyperiod.periodassignment

import com.acme.transport.crewing.clock.Clock
import com.acme.transport.kit.flags.Registry

/**
 * Moves strict duty period assignments through their life cycle and keeps a journal of what it did.
 */
data class StrictDutyPeriodAssignmentManager(
    var store: StrictDutyPeriodAssignmentStore,
    var validator: StrictDutyPeriodAssignmentValidator,
    var clock: Clock,
    var journal: MutableList<String>,
    var engine: StrictDutyPeriodAssignmentEngine,
) {
    private fun note(message: String) {
        journal.add("${clock.now()} $message")
    }

    fun submit(code: String) {
        val item = store.find(code) ?: error("strict duty period assignment $code not found")
        validator.requireValid(item)
        item.moveTo(StrictDutyPeriodAssignmentStatus.PENDING)
        store.save(item)
        note("$code is pending")
    }

    fun retire(code: String, reason: String) {
        val item = store.find(code) ?: error("strict duty period assignment $code not found")
        if (item.isTerminal()) {
            note("$code already closed")
            return
        }
        item.moveTo(StrictDutyPeriodAssignmentStatus.CLOSED)
        store.save(item)
        note("$code is closed: $reason")
    }

    fun score(code: String): Double {
        val item = store.find(code) ?: error("strict duty period assignment $code not found")
        val result = engine.run(item)
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

fun newStrictDutyPeriodAssignmentManager(store: StrictDutyPeriodAssignmentStore, clock: Clock, flags: Registry): StrictDutyPeriodAssignmentManager {
    return StrictDutyPeriodAssignmentManager(
        store = store,
        validator = defaultStrictDutyPeriodAssignmentValidator(),
        clock = clock,
        journal = mutableListOf<String>(),
        engine = defaultStrictDutyPeriodAssignmentEngine(flags),
    )
}
