package com.acme.transport.crewing.dutyperiod.schedule

import com.acme.transport.crewing.clock.Clock

/** Moves manual duty period slots through their life cycle and keeps a journal of what it did. */
data class ManualDutyPeriodSlotManager(
    var store: ManualDutyPeriodSlotStore,
    var validator: ManualDutyPeriodSlotValidator,
    var clock: Clock,
    var journal: MutableList<String>,
) {
    private fun note(message: String) {
        journal.add("${clock.now()} $message")
    }

    fun advance(code: String) {
        val item = store.find(code) ?: error("manual duty period slot $code not found")
        validator.requireValid(item)
        item.moveTo(ManualDutyPeriodSlotStatus.GRANTED)
        store.save(item)
        note("$code is granted")
    }

    fun finish(code: String, reason: String) {
        val item = store.find(code) ?: error("manual duty period slot $code not found")
        if (item.isTerminal()) {
            note("$code already closed")
            return
        }
        item.moveTo(ManualDutyPeriodSlotStatus.EXPIRED)
        store.save(item)
        note("$code is expired: $reason")
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

fun newManualDutyPeriodSlotManager(store: ManualDutyPeriodSlotStore, clock: Clock): ManualDutyPeriodSlotManager {
    return ManualDutyPeriodSlotManager(
        store = store,
        validator = defaultManualDutyPeriodSlotValidator(),
        clock = clock,
        journal = mutableListOf<String>(),
    )
}
