package com.acme.transport.terminal.dockdoor.plan

import com.acme.transport.terminal.clock.Clock

/** Moves strict dock door schedules through their life cycle and keeps a journal of what it did. */
data class StrictDockDoorScheduleDesk(
    var store: StrictDockDoorScheduleStore,
    var validator: StrictDockDoorScheduleValidator,
    var clock: Clock,
    var journal: MutableList<String>,
) {
    private fun note(message: String) {
        journal.add("${clock.now()} $message")
    }

    fun advance(code: String) {
        val item = store.find(code) ?: error("strict dock door schedule $code not found")
        validator.requireValid(item)
        item.moveTo(StrictDockDoorScheduleStatus.IN_REVIEW)
        store.save(item)
        note("$code is in review")
    }

    fun complete(code: String, reason: String) {
        val item = store.find(code) ?: error("strict dock door schedule $code not found")
        if (item.isTerminal()) {
            note("$code already closed")
            return
        }
        item.moveTo(StrictDockDoorScheduleStatus.ARCHIVED)
        store.save(item)
        note("$code is archived: $reason")
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

fun newStrictDockDoorScheduleDesk(store: StrictDockDoorScheduleStore, clock: Clock): StrictDockDoorScheduleDesk {
    return StrictDockDoorScheduleDesk(
        store = store,
        validator = defaultStrictDockDoorScheduleValidator(),
        clock = clock,
        journal = mutableListOf<String>(),
    )
}
