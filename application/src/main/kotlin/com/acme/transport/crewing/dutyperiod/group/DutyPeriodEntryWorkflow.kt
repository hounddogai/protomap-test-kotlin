package com.acme.transport.crewing.dutyperiod.group

import com.acme.transport.crewing.clock.Clock

/** Moves duty period entries through their life cycle and keeps a journal of what it did. */
data class DutyPeriodEntryService(
    var store: DutyPeriodEntryStore,
    var validator: DutyPeriodEntryValidator,
    var clock: Clock,
    var journal: MutableList<String>,
    var engine: DutyPeriodEntryEngine,
) {
    private fun note(message: String) {
        journal.add("${clock.now()} $message")
    }

    fun advance(code: String) {
        val item = store.find(code) ?: error("duty period entry $code not found")
        validator.requireValid(item)
        item.moveTo(DutyPeriodEntryStatus.PENDING)
        store.save(item)
        note("$code is pending")
    }

    fun finish(code: String, reason: String) {
        val item = store.find(code) ?: error("duty period entry $code not found")
        if (item.isTerminal()) {
            note("$code already closed")
            return
        }
        item.moveTo(DutyPeriodEntryStatus.CLOSED)
        store.save(item)
        note("$code is closed: $reason")
    }

    fun evaluate(code: String): Double {
        val item = store.find(code) ?: error("duty period entry $code not found")
        val result = engine.price(item)
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

fun newDutyPeriodEntryService(store: DutyPeriodEntryStore, clock: Clock): DutyPeriodEntryService {
    return DutyPeriodEntryService(
        store = store,
        validator = defaultDutyPeriodEntryValidator(),
        clock = clock,
        journal = mutableListOf<String>(),
        engine = defaultDutyPeriodEntryEngine(),
    )
}
