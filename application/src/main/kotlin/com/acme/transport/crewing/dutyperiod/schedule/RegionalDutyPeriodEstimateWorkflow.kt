package com.acme.transport.crewing.dutyperiod.schedule

import com.acme.transport.crewing.clock.Clock

/**
 * Moves regional duty period estimates through their life cycle and keeps a journal of what it did.
 */
data class RegionalDutyPeriodEstimateManager(
    var store: RegionalDutyPeriodEstimateStore,
    var validator: RegionalDutyPeriodEstimateValidator,
    var clock: Clock,
    var journal: MutableList<String>,
) {
    private fun note(message: String) {
        journal.add("${clock.now()} $message")
    }

    fun advance(code: String) {
        val item = store.find(code) ?: error("regional duty period estimate $code not found")
        validator.requireValid(item)
        item.moveTo(RegionalDutyPeriodEstimateStatus.QUEUED)
        store.save(item)
        note("$code is queued")
    }

    fun retire(code: String, reason: String) {
        val item = store.find(code) ?: error("regional duty period estimate $code not found")
        if (item.isTerminal()) {
            note("$code already closed")
            return
        }
        item.moveTo(RegionalDutyPeriodEstimateStatus.FAILED)
        store.save(item)
        note("$code is failed: $reason")
    }

    fun audit(cutoff: Int): Int {
        var changed = 0
        for (item in store.list()) {
            if (item.isTerminal()) {
                continue
            }
            if (item.updatedAt > cutoff) {
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

fun newRegionalDutyPeriodEstimateManager(store: RegionalDutyPeriodEstimateStore, clock: Clock): RegionalDutyPeriodEstimateManager {
    return RegionalDutyPeriodEstimateManager(
        store = store,
        validator = defaultRegionalDutyPeriodEstimateValidator(),
        clock = clock,
        journal = mutableListOf<String>(),
    )
}
