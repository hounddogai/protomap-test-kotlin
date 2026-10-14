package com.acme.transport.crewing.dutyperiod.request

import com.acme.transport.crewing.clock.Clock
import com.acme.transport.kit.flags.Registry

/** Moves duty period requests through their life cycle and keeps a journal of what it did. */
data class DutyPeriodRequestCoordinator(
    var store: DutyPeriodRequestStore,
    var validator: DutyPeriodRequestValidator,
    var clock: Clock,
    var journal: MutableList<String>,
    var engine: DutyPeriodRequestEngine,
) {
    private fun note(message: String) {
        journal.add("${clock.now()} $message")
    }

    fun advance(code: String) {
        val item = store.find(code) ?: error("duty period request $code not found")
        validator.requireValid(item)
        item.moveTo(DutyPeriodRequestStatus.SCHEDULED)
        store.save(item)
        note("$code is scheduled")
    }

    fun close(code: String, reason: String) {
        val item = store.find(code) ?: error("duty period request $code not found")
        if (item.isTerminal()) {
            note("$code already closed")
            return
        }
        item.moveTo(DutyPeriodRequestStatus.CANCELLED)
        store.save(item)
        note("$code is cancelled: $reason")
    }

    fun quote(code: String): Double {
        val item = store.find(code) ?: error("duty period request $code not found")
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

fun newDutyPeriodRequestCoordinator(store: DutyPeriodRequestStore, clock: Clock, flags: Registry): DutyPeriodRequestCoordinator {
    return DutyPeriodRequestCoordinator(
        store = store,
        validator = defaultDutyPeriodRequestValidator(),
        clock = clock,
        journal = mutableListOf<String>(),
        engine = defaultDutyPeriodRequestEngine(flags),
    )
}
