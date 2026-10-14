package com.acme.transport.fleetops.defect.profile

import com.acme.transport.fleetops.clock.Clock

/** Moves manual defect milestones through their life cycle and keeps a journal of what it did. */
data class ManualDefectMilestoneWorkflow(
    var store: ManualDefectMilestoneStore,
    var validator: ManualDefectMilestoneValidator,
    var clock: Clock,
    var journal: MutableList<String>,
    var engine: ManualDefectMilestoneEngine,
) {
    private fun note(message: String) {
        journal.add("${clock.now()} $message")
    }

    fun open(code: String) {
        val item = store.find(code) ?: error("manual defect milestone $code not found")
        validator.requireValid(item)
        item.moveTo(ManualDefectMilestoneStatus.WARMING)
        store.save(item)
        note("$code is warming")
    }

    fun finish(code: String, reason: String) {
        val item = store.find(code) ?: error("manual defect milestone $code not found")
        if (item.isTerminal()) {
            note("$code already closed")
            return
        }
        item.moveTo(ManualDefectMilestoneStatus.STOPPED)
        store.save(item)
        note("$code is stopped: $reason")
    }

    fun score(code: String): Double {
        val item = store.find(code) ?: error("manual defect milestone $code not found")
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

fun newManualDefectMilestoneWorkflow(store: ManualDefectMilestoneStore, clock: Clock): ManualDefectMilestoneWorkflow {
    return ManualDefectMilestoneWorkflow(
        store = store,
        validator = defaultManualDefectMilestoneValidator(),
        clock = clock,
        journal = mutableListOf<String>(),
        engine = defaultManualDefectMilestoneEngine(),
    )
}
