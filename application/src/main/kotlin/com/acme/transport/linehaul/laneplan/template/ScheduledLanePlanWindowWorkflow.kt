package com.acme.transport.linehaul.laneplan.template

import com.acme.transport.linehaul.clock.Clock

/** Moves scheduled lane plan windows through their life cycle and keeps a journal of what it did. */
data class ScheduledLanePlanWindowCoordinator(
    var store: ScheduledLanePlanWindowStore,
    var validator: ScheduledLanePlanWindowValidator,
    var clock: Clock,
    var journal: MutableList<String>,
) {
    private fun note(message: String) {
        journal.add("${clock.now()} $message")
    }

    fun advance(code: String) {
        val item = store.find(code) ?: error("scheduled lane plan window $code not found")
        validator.requireValid(item)
        item.moveTo(ScheduledLanePlanWindowStatus.QUEUED)
        store.save(item)
        note("$code is queued")
    }

    fun settle(code: String, reason: String) {
        val item = store.find(code) ?: error("scheduled lane plan window $code not found")
        if (item.isTerminal()) {
            note("$code already closed")
            return
        }
        item.moveTo(ScheduledLanePlanWindowStatus.FAILED)
        store.save(item)
        note("$code is failed: $reason")
    }

    fun scan(cutoff: Int): Int {
        var changed = 0
        for (item in store.list()) {
            if (item.isTerminal()) {
                continue
            }
            if (item.startsAt > cutoff) {
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

fun newScheduledLanePlanWindowCoordinator(store: ScheduledLanePlanWindowStore, clock: Clock): ScheduledLanePlanWindowCoordinator {
    return ScheduledLanePlanWindowCoordinator(
        store = store,
        validator = defaultScheduledLanePlanWindowValidator(),
        clock = clock,
        journal = mutableListOf<String>(),
    )
}
