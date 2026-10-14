package com.acme.transport.crewing.dutyperiod.baseline

import com.acme.transport.crewing.clock.Clock

/** Moves duty period estimates through their life cycle and keeps a journal of what it did. */
data class DutyPeriodEstimateManager(
    var store: DutyPeriodEstimateStore,
    var validator: DutyPeriodEstimateValidator,
    var clock: Clock,
    var journal: MutableList<String>,
) {
    private fun note(message: String) {
        journal.add("${clock.now()} $message")
    }

    fun submit(code: String) {
        val item = store.find(code) ?: error("duty period estimate $code not found")
        validator.requireValid(item)
        item.moveTo(DutyPeriodEstimateStatus.VALIDATED)
        store.save(item)
        note("$code is validated")
    }

    fun complete(code: String, reason: String) {
        val item = store.find(code) ?: error("duty period estimate $code not found")
        if (item.isTerminal()) {
            note("$code already closed")
            return
        }
        item.moveTo(DutyPeriodEstimateStatus.RETIRED)
        store.save(item)
        note("$code is retired: $reason")
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

fun newDutyPeriodEstimateManager(store: DutyPeriodEstimateStore, clock: Clock): DutyPeriodEstimateManager {
    return DutyPeriodEstimateManager(
        store = store,
        validator = defaultDutyPeriodEstimateValidator(),
        clock = clock,
        journal = mutableListOf<String>(),
    )
}
