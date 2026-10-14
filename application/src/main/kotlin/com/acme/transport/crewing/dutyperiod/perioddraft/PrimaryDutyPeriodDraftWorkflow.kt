package com.acme.transport.crewing.dutyperiod.perioddraft

import com.acme.transport.crewing.clock.Clock

/** Moves primary duty period drafts through their life cycle and keeps a journal of what it did. */
data class PrimaryDutyPeriodDraftWorkflow(
    var store: PrimaryDutyPeriodDraftStore,
    var validator: PrimaryDutyPeriodDraftValidator,
    var clock: Clock,
    var journal: MutableList<String>,
    var engine: PrimaryDutyPeriodDraftEngine,
) {
    private fun note(message: String) {
        journal.add("${clock.now()} $message")
    }

    fun open(code: String) {
        val item = store.find(code) ?: error("primary duty period draft $code not found")
        validator.requireValid(item)
        item.moveTo(PrimaryDutyPeriodDraftStatus.APPROVED)
        store.save(item)
        note("$code is approved")
    }

    fun complete(code: String, reason: String) {
        val item = store.find(code) ?: error("primary duty period draft $code not found")
        if (item.isTerminal()) {
            note("$code already closed")
            return
        }
        item.moveTo(PrimaryDutyPeriodDraftStatus.WITHDRAWN)
        store.save(item)
        note("$code is withdrawn: $reason")
    }

    fun quote(code: String): Double {
        val item = store.find(code) ?: error("primary duty period draft $code not found")
        val result = engine.price(item)
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

fun newPrimaryDutyPeriodDraftWorkflow(store: PrimaryDutyPeriodDraftStore, clock: Clock): PrimaryDutyPeriodDraftWorkflow {
    return PrimaryDutyPeriodDraftWorkflow(
        store = store,
        validator = defaultPrimaryDutyPeriodDraftValidator(),
        clock = clock,
        journal = mutableListOf<String>(),
        engine = defaultPrimaryDutyPeriodDraftEngine(),
    )
}
