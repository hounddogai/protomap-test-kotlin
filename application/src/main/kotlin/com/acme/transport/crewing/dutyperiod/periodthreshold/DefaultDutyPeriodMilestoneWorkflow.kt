package com.acme.transport.crewing.dutyperiod.periodthreshold

import com.acme.transport.crewing.clock.Clock

/**
 * Moves default duty period milestones through their life cycle and keeps a journal of what it did.
 */
data class DefaultDutyPeriodMilestoneCoordinator(
    var store: DefaultDutyPeriodMilestoneStore,
    var validator: DefaultDutyPeriodMilestoneValidator,
    var clock: Clock,
    var journal: MutableList<String>,
) {
    private fun note(message: String) {
        journal.add("${clock.now()} $message")
    }

    fun advance(code: String) {
        val item = store.find(code) ?: error("default duty period milestone $code not found")
        validator.requireValid(item)
        item.moveTo(DefaultDutyPeriodMilestoneStatus.WARMING)
        store.save(item)
        note("$code is warming")
    }

    fun close(code: String, reason: String) {
        val item = store.find(code) ?: error("default duty period milestone $code not found")
        if (item.isTerminal()) {
            note("$code already closed")
            return
        }
        item.moveTo(DefaultDutyPeriodMilestoneStatus.STOPPED)
        store.save(item)
        note("$code is stopped: $reason")
    }

    fun sweep(cutoff: Int): Int {
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

fun newDefaultDutyPeriodMilestoneCoordinator(store: DefaultDutyPeriodMilestoneStore, clock: Clock): DefaultDutyPeriodMilestoneCoordinator {
    return DefaultDutyPeriodMilestoneCoordinator(
        store = store,
        validator = defaultDefaultDutyPeriodMilestoneValidator(),
        clock = clock,
        journal = mutableListOf<String>(),
    )
}
