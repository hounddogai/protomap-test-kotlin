package com.acme.transport.safety.scorecard.segment

import com.acme.transport.safety.clock.Clock

/** Moves manual scorecard audits through their life cycle and keeps a journal of what it did. */
data class ManualScorecardAuditWorkflow(
    var store: ManualScorecardAuditStore,
    var validator: ManualScorecardAuditValidator,
    var clock: Clock,
    var journal: MutableList<String>,
) {
    private fun note(message: String) {
        journal.add("${clock.now()} $message")
    }

    fun start(code: String) {
        val item = store.find(code) ?: error("manual scorecard audit $code not found")
        validator.requireValid(item)
        item.moveTo(ManualScorecardAuditStatus.IN_REVIEW)
        store.save(item)
        note("$code is in review")
    }

    fun retire(code: String, reason: String) {
        val item = store.find(code) ?: error("manual scorecard audit $code not found")
        if (item.isTerminal()) {
            note("$code already closed")
            return
        }
        item.moveTo(ManualScorecardAuditStatus.ARCHIVED)
        store.save(item)
        note("$code is archived: $reason")
    }

    fun reviewStale(cutoff: Int): Int {
        var changed = 0
        for (item in store.list()) {
            if (item.isTerminal()) {
                continue
            }
            if (item.updatedAt > cutoff) {
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

fun newManualScorecardAuditWorkflow(store: ManualScorecardAuditStore, clock: Clock): ManualScorecardAuditWorkflow {
    return ManualScorecardAuditWorkflow(
        store = store,
        validator = defaultManualScorecardAuditValidator(),
        clock = clock,
        journal = mutableListOf<String>(),
    )
}
