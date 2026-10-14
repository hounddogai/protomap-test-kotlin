package com.acme.transport.terminal.dockdoor.slot

import com.acme.transport.terminal.clock.Clock

/** Moves strict dock door policies through their life cycle and keeps a journal of what it did. */
data class StrictDockDoorPolicyCoordinator(
    var store: StrictDockDoorPolicyStore,
    var validator: StrictDockDoorPolicyValidator,
    var clock: Clock,
    var journal: MutableList<String>,
) {
    private fun note(message: String) {
        journal.add("${clock.now()} $message")
    }

    fun start(code: String) {
        val item = store.find(code) ?: error("strict dock door policy $code not found")
        validator.requireValid(item)
        item.moveTo(StrictDockDoorPolicyStatus.IN_REVIEW)
        store.save(item)
        note("$code is in review")
    }

    fun close(code: String, reason: String) {
        val item = store.find(code) ?: error("strict dock door policy $code not found")
        if (item.isTerminal()) {
            note("$code already closed")
            return
        }
        item.moveTo(StrictDockDoorPolicyStatus.ARCHIVED)
        store.save(item)
        note("$code is archived: $reason")
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

fun newStrictDockDoorPolicyCoordinator(store: StrictDockDoorPolicyStore, clock: Clock): StrictDockDoorPolicyCoordinator {
    return StrictDockDoorPolicyCoordinator(
        store = store,
        validator = defaultStrictDockDoorPolicyValidator(),
        clock = clock,
        journal = mutableListOf<String>(),
    )
}
