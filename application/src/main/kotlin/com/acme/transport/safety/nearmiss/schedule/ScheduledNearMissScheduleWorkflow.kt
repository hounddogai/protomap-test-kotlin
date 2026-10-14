package com.acme.transport.safety.nearmiss.schedule

import com.acme.transport.safety.clock.Clock

/** Moves scheduled near miss schedules through their life cycle and keeps a journal of what it did. */
data class ScheduledNearMissScheduleWorkflow(
    var store: ScheduledNearMissScheduleStore,
    var validator: ScheduledNearMissScheduleValidator,
    var clock: Clock,
    var journal: MutableList<String>,
    var engine: ScheduledNearMissScheduleEngine,
) {
    private fun note(message: String) {
        journal.add("${clock.now()} $message")
    }

    fun submit(code: String) {
        val item = store.find(code) ?: error("scheduled near miss schedule $code not found")
        validator.requireValid(item)
        item.moveTo(ScheduledNearMissScheduleStatus.GRANTED)
        store.save(item)
        note("$code is granted")
    }

    fun settle(code: String, reason: String) {
        val item = store.find(code) ?: error("scheduled near miss schedule $code not found")
        if (item.isTerminal()) {
            note("$code already closed")
            return
        }
        item.moveTo(ScheduledNearMissScheduleStatus.EXPIRED)
        store.save(item)
        note("$code is expired: $reason")
    }

    fun quote(code: String): Double {
        val item = store.find(code) ?: error("scheduled near miss schedule $code not found")
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
            if (item.endsAt > cutoff) {
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

fun newScheduledNearMissScheduleWorkflow(store: ScheduledNearMissScheduleStore, clock: Clock): ScheduledNearMissScheduleWorkflow {
    return ScheduledNearMissScheduleWorkflow(
        store = store,
        validator = defaultScheduledNearMissScheduleValidator(),
        clock = clock,
        journal = mutableListOf<String>(),
        engine = defaultScheduledNearMissScheduleEngine(),
    )
}
