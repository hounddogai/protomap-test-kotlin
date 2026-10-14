package com.acme.transport.terminal.dwell.dwellmilestone

import com.acme.transport.terminal.clock.Clock

/** Moves strict dwell plans through their life cycle and keeps a journal of what it did. */
data class StrictDwellPlanDesk(
    var store: StrictDwellPlanStore,
    var validator: StrictDwellPlanValidator,
    var clock: Clock,
    var journal: MutableList<String>,
) {
    private fun note(message: String) {
        journal.add("${clock.now()} $message")
    }

    fun open(code: String) {
        val item = store.find(code) ?: error("strict dwell plan $code not found")
        validator.requireValid(item)
        item.moveTo(StrictDwellPlanStatus.APPROVED)
        store.save(item)
        note("$code is approved")
    }

    fun close(code: String, reason: String) {
        val item = store.find(code) ?: error("strict dwell plan $code not found")
        if (item.isTerminal()) {
            note("$code already closed")
            return
        }
        item.moveTo(StrictDwellPlanStatus.WITHDRAWN)
        store.save(item)
        note("$code is withdrawn: $reason")
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

fun newStrictDwellPlanDesk(store: StrictDwellPlanStore, clock: Clock): StrictDwellPlanDesk {
    return StrictDwellPlanDesk(
        store = store,
        validator = defaultStrictDwellPlanValidator(),
        clock = clock,
        journal = mutableListOf<String>(),
    )
}
