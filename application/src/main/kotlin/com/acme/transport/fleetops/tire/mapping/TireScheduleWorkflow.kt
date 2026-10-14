package com.acme.transport.fleetops.tire.mapping

import com.acme.transport.fleetops.clock.Clock

/** Moves tire schedules through their life cycle and keeps a journal of what it did. */
data class TireScheduleWorkflow(
    var store: TireScheduleStore,
    var validator: TireScheduleValidator,
    var clock: Clock,
    var journal: MutableList<String>,
) {
    private fun note(message: String) {
        journal.add("${clock.now()} $message")
    }

    fun open(code: String) {
        val item = store.find(code) ?: error("tire schedule $code not found")
        validator.requireValid(item)
        item.moveTo(TireScheduleStatus.WARMING)
        store.save(item)
        note("$code is warming")
    }

    fun retire(code: String, reason: String) {
        val item = store.find(code) ?: error("tire schedule $code not found")
        if (item.isTerminal()) {
            note("$code already closed")
            return
        }
        item.moveTo(TireScheduleStatus.STOPPED)
        store.save(item)
        note("$code is stopped: $reason")
    }

    fun sweep(cutoff: Int): Int {
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

fun newTireScheduleWorkflow(store: TireScheduleStore, clock: Clock): TireScheduleWorkflow {
    return TireScheduleWorkflow(
        store = store,
        validator = defaultTireScheduleValidator(),
        clock = clock,
        journal = mutableListOf<String>(),
    )
}
