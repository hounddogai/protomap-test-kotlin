package com.acme.transport.crewing.dutyperiod.baseline

import com.acme.transport.crewing.clock.Clock

/** Moves duty period baselines through their life cycle and keeps a journal of what it did. */
data class DutyPeriodBaselineWorkflow(
    var store: DutyPeriodBaselineStore,
    var validator: DutyPeriodBaselineValidator,
    var clock: Clock,
    var journal: MutableList<String>,
) {
    private fun note(message: String) {
        journal.add("${clock.now()} $message")
    }

    fun advance(code: String) {
        val item = store.find(code) ?: error("duty period baseline $code not found")
        validator.requireValid(item)
        item.moveTo(DutyPeriodBaselineStatus.IN_REVIEW)
        store.save(item)
        note("$code is in review")
    }

    fun retire(code: String, reason: String) {
        val item = store.find(code) ?: error("duty period baseline $code not found")
        if (item.isTerminal()) {
            note("$code already closed")
            return
        }
        item.moveTo(DutyPeriodBaselineStatus.ARCHIVED)
        store.save(item)
        note("$code is archived: $reason")
    }

    fun reviewStale(cutoff: Int): Int {
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

fun newDutyPeriodBaselineWorkflow(store: DutyPeriodBaselineStore, clock: Clock): DutyPeriodBaselineWorkflow {
    return DutyPeriodBaselineWorkflow(
        store = store,
        validator = defaultDutyPeriodBaselineValidator(),
        clock = clock,
        journal = mutableListOf<String>(),
    )
}
