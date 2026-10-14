package com.acme.transport.crewing.dutyperiod.manualperiodassignment

import com.acme.transport.crewing.clock.Clock
import com.acme.transport.kit.flags.Registry

/**
 * Moves capped duty period assignments through their life cycle and keeps a journal of what it did.
 */
data class CappedDutyPeriodAssignmentWorkflow(
    var store: CappedDutyPeriodAssignmentStore,
    var validator: CappedDutyPeriodAssignmentValidator,
    var clock: Clock,
    var journal: MutableList<String>,
    var engine: CappedDutyPeriodAssignmentEngine,
) {
    private fun note(message: String) {
        journal.add("${clock.now()} $message")
    }

    fun advance(code: String) {
        val item = store.find(code) ?: error("capped duty period assignment $code not found")
        validator.requireValid(item)
        item.moveTo(CappedDutyPeriodAssignmentStatus.SCHEDULED)
        store.save(item)
        note("$code is scheduled")
    }

    fun close(code: String, reason: String) {
        val item = store.find(code) ?: error("capped duty period assignment $code not found")
        if (item.isTerminal()) {
            note("$code already closed")
            return
        }
        item.moveTo(CappedDutyPeriodAssignmentStatus.CANCELLED)
        store.save(item)
        note("$code is cancelled: $reason")
    }

    fun score(code: String): Double {
        val item = store.find(code) ?: error("capped duty period assignment $code not found")
        val result = engine.evaluate(item)
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

fun newCappedDutyPeriodAssignmentWorkflow(store: CappedDutyPeriodAssignmentStore, clock: Clock, flags: Registry): CappedDutyPeriodAssignmentWorkflow {
    return CappedDutyPeriodAssignmentWorkflow(
        store = store,
        validator = defaultCappedDutyPeriodAssignmentValidator(),
        clock = clock,
        journal = mutableListOf<String>(),
        engine = defaultCappedDutyPeriodAssignmentEngine(flags),
    )
}
