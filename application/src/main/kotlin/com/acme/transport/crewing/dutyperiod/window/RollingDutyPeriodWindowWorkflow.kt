package com.acme.transport.crewing.dutyperiod.window

import com.acme.transport.crewing.clock.Clock

/** Moves rolling duty period windows through their life cycle and keeps a journal of what it did. */
data class RollingDutyPeriodWindowService(
    var store: RollingDutyPeriodWindowStore,
    var validator: RollingDutyPeriodWindowValidator,
    var clock: Clock,
    var journal: MutableList<String>,
) {
    private fun note(message: String) {
        journal.add("${clock.now()} $message")
    }

    fun advance(code: String) {
        val item = store.find(code) ?: error("rolling duty period window $code not found")
        validator.requireValid(item)
        item.moveTo(RollingDutyPeriodWindowStatus.PENDING)
        store.save(item)
        note("$code is pending")
    }

    fun complete(code: String, reason: String) {
        val item = store.find(code) ?: error("rolling duty period window $code not found")
        if (item.isTerminal()) {
            note("$code already closed")
            return
        }
        item.moveTo(RollingDutyPeriodWindowStatus.CLOSED)
        store.save(item)
        note("$code is closed: $reason")
    }

    fun sweep(cutoff: Int): Int {
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

fun newRollingDutyPeriodWindowService(store: RollingDutyPeriodWindowStore, clock: Clock): RollingDutyPeriodWindowService {
    return RollingDutyPeriodWindowService(
        store = store,
        validator = defaultRollingDutyPeriodWindowValidator(),
        clock = clock,
        journal = mutableListOf<String>(),
    )
}
