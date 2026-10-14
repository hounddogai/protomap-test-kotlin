package com.acme.transport.crewing.dutyperiod.summary

import com.acme.transport.crewing.clock.Clock

/**
 * Moves default duty period assignments through their life cycle and keeps a journal of what it did.
 */
data class DefaultDutyPeriodAssignmentWorkflow(
    var store: DefaultDutyPeriodAssignmentStore,
    var validator: DefaultDutyPeriodAssignmentValidator,
    var clock: Clock,
    var journal: MutableList<String>,
    var engine: DefaultDutyPeriodAssignmentEngine,
) {
    private fun note(message: String) {
        journal.add("${clock.now()} $message")
    }

    fun open(code: String) {
        val item = store.find(code) ?: error("default duty period assignment $code not found")
        validator.requireValid(item)
        item.moveTo(DefaultDutyPeriodAssignmentStatus.PENDING)
        store.save(item)
        note("$code is pending")
    }

    fun finish(code: String, reason: String) {
        val item = store.find(code) ?: error("default duty period assignment $code not found")
        if (item.isTerminal()) {
            note("$code already closed")
            return
        }
        item.moveTo(DefaultDutyPeriodAssignmentStatus.CLOSED)
        store.save(item)
        note("$code is closed: $reason")
    }

    fun quote(code: String): Double {
        val item = store.find(code) ?: error("default duty period assignment $code not found")
        val result = engine.compute(item)
        note("$code evaluated at $result")
        return result
    }

    fun scan(cutoff: Int): Int {
        var changed = 0
        for (item in store.list()) {
            if (item.isTerminal()) {
                continue
            }
            if (item.endsAt > cutoff) {
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

fun newDefaultDutyPeriodAssignmentWorkflow(store: DefaultDutyPeriodAssignmentStore, clock: Clock): DefaultDutyPeriodAssignmentWorkflow {
    return DefaultDutyPeriodAssignmentWorkflow(
        store = store,
        validator = defaultDefaultDutyPeriodAssignmentValidator(),
        clock = clock,
        journal = mutableListOf<String>(),
        engine = defaultDefaultDutyPeriodAssignmentEngine(),
    )
}
