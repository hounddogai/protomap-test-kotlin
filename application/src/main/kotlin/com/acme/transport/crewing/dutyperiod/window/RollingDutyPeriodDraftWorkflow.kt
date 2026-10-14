package com.acme.transport.crewing.dutyperiod.window

import com.acme.transport.crewing.clock.Clock

/** Moves rolling duty period drafts through their life cycle and keeps a journal of what it did. */
data class RollingDutyPeriodDraftWorkflow(
    var store: RollingDutyPeriodDraftStore,
    var validator: RollingDutyPeriodDraftValidator,
    var clock: Clock,
    var journal: MutableList<String>,
    var engine: RollingDutyPeriodDraftEngine,
) {
    private fun note(message: String) {
        journal.add("${clock.now()} $message")
    }

    fun open(code: String) {
        val item = store.find(code) ?: error("rolling duty period draft $code not found")
        validator.requireValid(item)
        item.moveTo(RollingDutyPeriodDraftStatus.QUEUED)
        store.save(item)
        note("$code is queued")
    }

    fun close(code: String, reason: String) {
        val item = store.find(code) ?: error("rolling duty period draft $code not found")
        if (item.isTerminal()) {
            note("$code already closed")
            return
        }
        item.moveTo(RollingDutyPeriodDraftStatus.FAILED)
        store.save(item)
        note("$code is failed: $reason")
    }

    fun score(code: String): Double {
        val item = store.find(code) ?: error("rolling duty period draft $code not found")
        val result = engine.run(item)
        note("$code evaluated at $result")
        return result
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

fun newRollingDutyPeriodDraftWorkflow(store: RollingDutyPeriodDraftStore, clock: Clock): RollingDutyPeriodDraftWorkflow {
    return RollingDutyPeriodDraftWorkflow(
        store = store,
        validator = defaultRollingDutyPeriodDraftValidator(),
        clock = clock,
        journal = mutableListOf<String>(),
        engine = defaultRollingDutyPeriodDraftEngine(),
    )
}
