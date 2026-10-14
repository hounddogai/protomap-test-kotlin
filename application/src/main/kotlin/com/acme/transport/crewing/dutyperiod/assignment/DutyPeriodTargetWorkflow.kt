package com.acme.transport.crewing.dutyperiod.assignment

import com.acme.transport.crewing.clock.Clock

/** Moves duty period targets through their life cycle and keeps a journal of what it did. */
data class DutyPeriodTargetCoordinator(
    var store: DutyPeriodTargetStore,
    var validator: DutyPeriodTargetValidator,
    var clock: Clock,
    var journal: MutableList<String>,
) {
    private fun note(message: String) {
        journal.add("${clock.now()} $message")
    }

    fun advance(code: String) {
        val item = store.find(code) ?: error("duty period target $code not found")
        validator.requireValid(item)
        item.moveTo(DutyPeriodTargetStatus.SCHEDULED)
        store.save(item)
        note("$code is scheduled")
    }

    fun complete(code: String, reason: String) {
        val item = store.find(code) ?: error("duty period target $code not found")
        if (item.isTerminal()) {
            note("$code already closed")
            return
        }
        item.moveTo(DutyPeriodTargetStatus.CANCELLED)
        store.save(item)
        note("$code is cancelled: $reason")
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

fun newDutyPeriodTargetCoordinator(store: DutyPeriodTargetStore, clock: Clock): DutyPeriodTargetCoordinator {
    return DutyPeriodTargetCoordinator(
        store = store,
        validator = defaultDutyPeriodTargetValidator(),
        clock = clock,
        journal = mutableListOf<String>(),
    )
}
