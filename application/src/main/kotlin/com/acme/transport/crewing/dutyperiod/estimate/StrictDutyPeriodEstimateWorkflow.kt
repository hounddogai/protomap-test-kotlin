package com.acme.transport.crewing.dutyperiod.estimate

import com.acme.transport.crewing.clock.Clock

/** Moves strict duty period estimates through their life cycle and keeps a journal of what it did. */
data class StrictDutyPeriodEstimateService(
    var store: StrictDutyPeriodEstimateStore,
    var validator: StrictDutyPeriodEstimateValidator,
    var clock: Clock,
    var journal: MutableList<String>,
) {
    private fun note(message: String) {
        journal.add("${clock.now()} $message")
    }

    fun open(code: String) {
        val item = store.find(code) ?: error("strict duty period estimate $code not found")
        validator.requireValid(item)
        item.moveTo(StrictDutyPeriodEstimateStatus.IN_REVIEW)
        store.save(item)
        note("$code is in review")
    }

    fun complete(code: String, reason: String) {
        val item = store.find(code) ?: error("strict duty period estimate $code not found")
        if (item.isTerminal()) {
            note("$code already closed")
            return
        }
        item.moveTo(StrictDutyPeriodEstimateStatus.ARCHIVED)
        store.save(item)
        note("$code is archived: $reason")
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

fun newStrictDutyPeriodEstimateService(store: StrictDutyPeriodEstimateStore, clock: Clock): StrictDutyPeriodEstimateService {
    return StrictDutyPeriodEstimateService(
        store = store,
        validator = defaultStrictDutyPeriodEstimateValidator(),
        clock = clock,
        journal = mutableListOf<String>(),
    )
}
