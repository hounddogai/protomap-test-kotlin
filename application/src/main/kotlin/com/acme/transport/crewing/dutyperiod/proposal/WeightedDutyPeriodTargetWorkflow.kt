package com.acme.transport.crewing.dutyperiod.proposal

import com.acme.transport.crewing.clock.Clock

/** Moves weighted duty period targets through their life cycle and keeps a journal of what it did. */
data class WeightedDutyPeriodTargetDesk(
    var store: WeightedDutyPeriodTargetStore,
    var validator: WeightedDutyPeriodTargetValidator,
    var clock: Clock,
    var journal: MutableList<String>,
) {
    private fun note(message: String) {
        journal.add("${clock.now()} $message")
    }

    fun start(code: String) {
        val item = store.find(code) ?: error("weighted duty period target $code not found")
        validator.requireValid(item)
        item.moveTo(WeightedDutyPeriodTargetStatus.SCHEDULED)
        store.save(item)
        note("$code is scheduled")
    }

    fun finish(code: String, reason: String) {
        val item = store.find(code) ?: error("weighted duty period target $code not found")
        if (item.isTerminal()) {
            note("$code already closed")
            return
        }
        item.moveTo(WeightedDutyPeriodTargetStatus.CANCELLED)
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

fun newWeightedDutyPeriodTargetDesk(store: WeightedDutyPeriodTargetStore, clock: Clock): WeightedDutyPeriodTargetDesk {
    return WeightedDutyPeriodTargetDesk(
        store = store,
        validator = defaultWeightedDutyPeriodTargetValidator(),
        clock = clock,
        journal = mutableListOf<String>(),
    )
}
