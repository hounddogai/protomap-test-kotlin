package com.acme.transport.crewing.dutyperiod.periodassignment

import com.acme.transport.crewing.clock.Clock

/** Moves duty period cycles through their life cycle and keeps a journal of what it did. */
data class DutyPeriodCycleManager(
    var store: DutyPeriodCycleStore,
    var validator: DutyPeriodCycleValidator,
    var clock: Clock,
    var journal: MutableList<String>,
) {
    private fun note(message: String) {
        journal.add("${clock.now()} $message")
    }

    fun start(code: String) {
        val item = store.find(code) ?: error("duty period cycle $code not found")
        validator.requireValid(item)
        item.moveTo(DutyPeriodCycleStatus.WARMING)
        store.save(item)
        note("$code is warming")
    }

    fun retire(code: String, reason: String) {
        val item = store.find(code) ?: error("duty period cycle $code not found")
        if (item.isTerminal()) {
            note("$code already closed")
            return
        }
        item.moveTo(DutyPeriodCycleStatus.STOPPED)
        store.save(item)
        note("$code is stopped: $reason")
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

fun newDutyPeriodCycleManager(store: DutyPeriodCycleStore, clock: Clock): DutyPeriodCycleManager {
    return DutyPeriodCycleManager(
        store = store,
        validator = defaultDutyPeriodCycleValidator(),
        clock = clock,
        journal = mutableListOf<String>(),
    )
}
