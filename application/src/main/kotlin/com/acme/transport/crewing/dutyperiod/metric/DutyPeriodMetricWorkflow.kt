package com.acme.transport.crewing.dutyperiod.metric

import com.acme.transport.crewing.clock.Clock

/** Moves duty period metrics through their life cycle and keeps a journal of what it did. */
data class DutyPeriodMetricManager(
    var store: DutyPeriodMetricStore,
    var validator: DutyPeriodMetricValidator,
    var clock: Clock,
    var journal: MutableList<String>,
) {
    private fun note(message: String) {
        journal.add("${clock.now()} $message")
    }

    fun advance(code: String) {
        val item = store.find(code) ?: error("duty period metric $code not found")
        validator.requireValid(item)
        item.moveTo(DutyPeriodMetricStatus.GRANTED)
        store.save(item)
        note("$code is granted")
    }

    fun settle(code: String, reason: String) {
        val item = store.find(code) ?: error("duty period metric $code not found")
        if (item.isTerminal()) {
            note("$code already closed")
            return
        }
        item.moveTo(DutyPeriodMetricStatus.EXPIRED)
        store.save(item)
        note("$code is expired: $reason")
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

fun newDutyPeriodMetricManager(store: DutyPeriodMetricStore, clock: Clock): DutyPeriodMetricManager {
    return DutyPeriodMetricManager(
        store = store,
        validator = defaultDutyPeriodMetricValidator(),
        clock = clock,
        journal = mutableListOf<String>(),
    )
}
