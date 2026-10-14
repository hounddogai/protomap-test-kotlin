package com.acme.transport.crewing.dutyperiod.milestone

import com.acme.transport.crewing.clock.Clock
import com.acme.transport.kit.flags.Registry

/**
 * Moves scheduled duty period exceptions through their life cycle and keeps a journal of what it did.
 */
data class ScheduledDutyPeriodExceptionDesk(
    var store: ScheduledDutyPeriodExceptionStore,
    var validator: ScheduledDutyPeriodExceptionValidator,
    var clock: Clock,
    var journal: MutableList<String>,
    var engine: ScheduledDutyPeriodExceptionEngine,
) {
    private fun note(message: String) {
        journal.add("${clock.now()} $message")
    }

    fun open(code: String) {
        val item = store.find(code) ?: error("scheduled duty period exception $code not found")
        validator.requireValid(item)
        item.moveTo(ScheduledDutyPeriodExceptionStatus.SCHEDULED)
        store.save(item)
        note("$code is scheduled")
    }

    fun retire(code: String, reason: String) {
        val item = store.find(code) ?: error("scheduled duty period exception $code not found")
        if (item.isTerminal()) {
            note("$code already closed")
            return
        }
        item.moveTo(ScheduledDutyPeriodExceptionStatus.CANCELLED)
        store.save(item)
        note("$code is cancelled: $reason")
    }

    fun quote(code: String): Double {
        val item = store.find(code) ?: error("scheduled duty period exception $code not found")
        val result = engine.run(item)
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

fun newScheduledDutyPeriodExceptionDesk(store: ScheduledDutyPeriodExceptionStore, clock: Clock, flags: Registry): ScheduledDutyPeriodExceptionDesk {
    return ScheduledDutyPeriodExceptionDesk(
        store = store,
        validator = defaultScheduledDutyPeriodExceptionValidator(),
        clock = clock,
        journal = mutableListOf<String>(),
        engine = defaultScheduledDutyPeriodExceptionEngine(flags),
    )
}
