package com.acme.transport.crewing.dutyperiod.proposal

import com.acme.transport.crewing.clock.Clock

/** Moves duty period proposals through their life cycle and keeps a journal of what it did. */
data class DutyPeriodProposalDesk(
    var store: DutyPeriodProposalStore,
    var validator: DutyPeriodProposalValidator,
    var clock: Clock,
    var journal: MutableList<String>,
) {
    private fun note(message: String) {
        journal.add("${clock.now()} $message")
    }

    fun open(code: String) {
        val item = store.find(code) ?: error("duty period proposal $code not found")
        validator.requireValid(item)
        item.moveTo(DutyPeriodProposalStatus.APPROVED)
        store.save(item)
        note("$code is approved")
    }

    fun retire(code: String, reason: String) {
        val item = store.find(code) ?: error("duty period proposal $code not found")
        if (item.isTerminal()) {
            note("$code already closed")
            return
        }
        item.moveTo(DutyPeriodProposalStatus.WITHDRAWN)
        store.save(item)
        note("$code is withdrawn: $reason")
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

fun newDutyPeriodProposalDesk(store: DutyPeriodProposalStore, clock: Clock): DutyPeriodProposalDesk {
    return DutyPeriodProposalDesk(
        store = store,
        validator = defaultDutyPeriodProposalValidator(),
        clock = clock,
        journal = mutableListOf<String>(),
    )
}
