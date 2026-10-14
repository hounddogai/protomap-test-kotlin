package com.acme.transport.crewing.dutyperiod.agreement

import com.acme.transport.crewing.clock.Clock

/** Moves duty period rules through their life cycle and keeps a journal of what it did. */
data class DutyPeriodRuleWorkflow(
    var store: DutyPeriodRuleStore,
    var validator: DutyPeriodRuleValidator,
    var clock: Clock,
    var journal: MutableList<String>,
) {
    private fun note(message: String) {
        journal.add("${clock.now()} $message")
    }

    fun start(code: String) {
        val item = store.find(code) ?: error("duty period rule $code not found")
        validator.requireValid(item)
        item.moveTo(DutyPeriodRuleStatus.SCHEDULED)
        store.save(item)
        note("$code is scheduled")
    }

    fun settle(code: String, reason: String) {
        val item = store.find(code) ?: error("duty period rule $code not found")
        if (item.isTerminal()) {
            note("$code already closed")
            return
        }
        item.moveTo(DutyPeriodRuleStatus.CANCELLED)
        store.save(item)
        note("$code is cancelled: $reason")
    }

    fun reviewStale(cutoff: Int): Int {
        var changed = 0
        for (item in store.list()) {
            if (item.isTerminal()) {
                continue
            }
            if (item.expiresAt > cutoff) {
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

fun newDutyPeriodRuleWorkflow(store: DutyPeriodRuleStore, clock: Clock): DutyPeriodRuleWorkflow {
    return DutyPeriodRuleWorkflow(
        store = store,
        validator = defaultDutyPeriodRuleValidator(),
        clock = clock,
        journal = mutableListOf<String>(),
    )
}
