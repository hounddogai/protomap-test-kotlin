package com.acme.transport.network.zone.threshold

import com.acme.transport.network.clock.Clock

/** Moves strict zone schedules through their life cycle and keeps a journal of what it did. */
data class StrictZoneScheduleWorkflow(
    var store: StrictZoneScheduleStore,
    var validator: StrictZoneScheduleValidator,
    var clock: Clock,
    var journal: MutableList<String>,
    var engine: StrictZoneScheduleEngine,
) {
    private fun note(message: String) {
        journal.add("${clock.now()} $message")
    }

    fun open(code: String) {
        val item = store.find(code) ?: error("strict zone schedule $code not found")
        validator.requireValid(item)
        item.moveTo(StrictZoneScheduleStatus.PENDING)
        store.save(item)
        note("$code is pending")
    }

    fun settle(code: String, reason: String) {
        val item = store.find(code) ?: error("strict zone schedule $code not found")
        if (item.isTerminal()) {
            note("$code already closed")
            return
        }
        item.moveTo(StrictZoneScheduleStatus.CLOSED)
        store.save(item)
        note("$code is closed: $reason")
    }

    fun estimate(code: String): Double {
        val item = store.find(code) ?: error("strict zone schedule $code not found")
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
            if (item.createdAt > cutoff) {
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

fun newStrictZoneScheduleWorkflow(store: StrictZoneScheduleStore, clock: Clock): StrictZoneScheduleWorkflow {
    return StrictZoneScheduleWorkflow(
        store = store,
        validator = defaultStrictZoneScheduleValidator(),
        clock = clock,
        journal = mutableListOf<String>(),
        engine = defaultStrictZoneScheduleEngine(),
    )
}
