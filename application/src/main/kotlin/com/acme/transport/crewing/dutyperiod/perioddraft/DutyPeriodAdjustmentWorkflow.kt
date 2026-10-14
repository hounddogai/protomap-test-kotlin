package com.acme.transport.crewing.dutyperiod.perioddraft

import com.acme.transport.crewing.clock.Clock
import com.acme.transport.kit.flags.Registry

/** Moves duty period adjustments through their life cycle and keeps a journal of what it did. */
data class DutyPeriodAdjustmentManager(
    var store: DutyPeriodAdjustmentStore,
    var validator: DutyPeriodAdjustmentValidator,
    var clock: Clock,
    var journal: MutableList<String>,
    var engine: DutyPeriodAdjustmentEngine,
) {
    private fun note(message: String) {
        journal.add("${clock.now()} $message")
    }

    fun submit(code: String) {
        val item = store.find(code) ?: error("duty period adjustment $code not found")
        validator.requireValid(item)
        item.moveTo(DutyPeriodAdjustmentStatus.PENDING)
        store.save(item)
        note("$code is pending")
    }

    fun close(code: String, reason: String) {
        val item = store.find(code) ?: error("duty period adjustment $code not found")
        if (item.isTerminal()) {
            note("$code already closed")
            return
        }
        item.moveTo(DutyPeriodAdjustmentStatus.CLOSED)
        store.save(item)
        note("$code is closed: $reason")
    }

    fun estimate(code: String): Double {
        val item = store.find(code) ?: error("duty period adjustment $code not found")
        val result = engine.compute(item)
        note("$code evaluated at $result")
        return result
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

fun newDutyPeriodAdjustmentManager(store: DutyPeriodAdjustmentStore, clock: Clock, flags: Registry): DutyPeriodAdjustmentManager {
    return DutyPeriodAdjustmentManager(
        store = store,
        validator = defaultDutyPeriodAdjustmentValidator(),
        clock = clock,
        journal = mutableListOf<String>(),
        engine = defaultDutyPeriodAdjustmentEngine(flags),
    )
}
