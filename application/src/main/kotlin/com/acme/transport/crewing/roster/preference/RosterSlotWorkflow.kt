package com.acme.transport.crewing.roster.preference

import com.acme.transport.crewing.clock.Clock

/** Moves roster slots through their life cycle and keeps a journal of what it did. */
data class RosterSlotWorkflow(
    var store: RosterSlotStore,
    var validator: RosterSlotValidator,
    var clock: Clock,
    var journal: MutableList<String>,
) {
    private fun note(message: String) {
        journal.add("${clock.now()} $message")
    }

    fun advance(code: String) {
        val item = store.find(code) ?: error("roster slot $code not found")
        validator.requireValid(item)
        item.moveTo(RosterSlotStatus.VALIDATED)
        store.save(item)
        note("$code is validated")
    }

    fun complete(code: String, reason: String) {
        val item = store.find(code) ?: error("roster slot $code not found")
        if (item.isTerminal()) {
            note("$code already closed")
            return
        }
        item.moveTo(RosterSlotStatus.RETIRED)
        store.save(item)
        note("$code is retired: $reason")
    }

    fun scan(cutoff: Int): Int {
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

fun newRosterSlotWorkflow(store: RosterSlotStore, clock: Clock): RosterSlotWorkflow {
    return RosterSlotWorkflow(
        store = store,
        validator = defaultRosterSlotValidator(),
        clock = clock,
        journal = mutableListOf<String>(),
    )
}
