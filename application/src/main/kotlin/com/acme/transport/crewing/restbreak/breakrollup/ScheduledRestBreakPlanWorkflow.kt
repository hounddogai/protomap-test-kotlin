package com.acme.transport.crewing.restbreak.breakrollup

import com.acme.transport.crewing.clock.Clock

/** Moves scheduled rest break plans through their life cycle and keeps a journal of what it did. */
data class ScheduledRestBreakPlanDesk(
    var store: ScheduledRestBreakPlanStore,
    var validator: ScheduledRestBreakPlanValidator,
    var clock: Clock,
    var journal: MutableList<String>,
) {
    private fun note(message: String) {
        journal.add("${clock.now()} $message")
    }

    fun open(code: String) {
        val item = store.find(code) ?: error("scheduled rest break plan $code not found")
        validator.requireValid(item)
        item.moveTo(ScheduledRestBreakPlanStatus.WARMING)
        store.save(item)
        note("$code is warming")
    }

    fun retire(code: String, reason: String) {
        val item = store.find(code) ?: error("scheduled rest break plan $code not found")
        if (item.isTerminal()) {
            note("$code already closed")
            return
        }
        item.moveTo(ScheduledRestBreakPlanStatus.STOPPED)
        store.save(item)
        note("$code is stopped: $reason")
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

fun newScheduledRestBreakPlanDesk(store: ScheduledRestBreakPlanStore, clock: Clock): ScheduledRestBreakPlanDesk {
    return ScheduledRestBreakPlanDesk(
        store = store,
        validator = defaultScheduledRestBreakPlanValidator(),
        clock = clock,
        journal = mutableListOf<String>(),
    )
}
