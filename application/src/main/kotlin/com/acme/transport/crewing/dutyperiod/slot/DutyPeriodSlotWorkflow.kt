package com.acme.transport.crewing.dutyperiod.slot

import com.acme.transport.crewing.clock.Clock

/** Moves duty period slots through their life cycle and keeps a journal of what it did. */
data class DutyPeriodSlotManager(
    var store: DutyPeriodSlotStore,
    var validator: DutyPeriodSlotValidator,
    var clock: Clock,
    var journal: MutableList<String>,
    var engine: DutyPeriodSlotEngine,
) {
    private fun note(message: String) {
        journal.add("${clock.now()} $message")
    }

    fun submit(code: String) {
        val item = store.find(code) ?: error("duty period slot $code not found")
        validator.requireValid(item)
        item.moveTo(DutyPeriodSlotStatus.SCHEDULED)
        store.save(item)
        note("$code is scheduled")
    }

    fun finish(code: String, reason: String) {
        val item = store.find(code) ?: error("duty period slot $code not found")
        if (item.isTerminal()) {
            note("$code already closed")
            return
        }
        item.moveTo(DutyPeriodSlotStatus.CANCELLED)
        store.save(item)
        note("$code is cancelled: $reason")
    }

    fun evaluate(code: String): Double {
        val item = store.find(code) ?: error("duty period slot $code not found")
        val result = engine.run(item)
        note("$code evaluated at $result")
        return result
    }

    fun audit(cutoff: Int): Int {
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

fun newDutyPeriodSlotManager(store: DutyPeriodSlotStore, clock: Clock): DutyPeriodSlotManager {
    return DutyPeriodSlotManager(
        store = store,
        validator = defaultDutyPeriodSlotValidator(),
        clock = clock,
        journal = mutableListOf<String>(),
        engine = defaultDutyPeriodSlotEngine(),
    )
}
