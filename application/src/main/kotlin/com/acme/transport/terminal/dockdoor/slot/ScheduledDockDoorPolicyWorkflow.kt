package com.acme.transport.terminal.dockdoor.slot

import com.acme.transport.terminal.clock.Clock

/** Moves scheduled dock door policies through their life cycle and keeps a journal of what it did. */
data class ScheduledDockDoorPolicyService(
    var store: ScheduledDockDoorPolicyStore,
    var validator: ScheduledDockDoorPolicyValidator,
    var clock: Clock,
    var journal: MutableList<String>,
) {
    private fun note(message: String) {
        journal.add("${clock.now()} $message")
    }

    fun submit(code: String) {
        val item = store.find(code) ?: error("scheduled dock door policy $code not found")
        validator.requireValid(item)
        item.moveTo(ScheduledDockDoorPolicyStatus.APPROVED)
        store.save(item)
        note("$code is approved")
    }

    fun close(code: String, reason: String) {
        val item = store.find(code) ?: error("scheduled dock door policy $code not found")
        if (item.isTerminal()) {
            note("$code already closed")
            return
        }
        item.moveTo(ScheduledDockDoorPolicyStatus.WITHDRAWN)
        store.save(item)
        note("$code is withdrawn: $reason")
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

fun newScheduledDockDoorPolicyService(store: ScheduledDockDoorPolicyStore, clock: Clock): ScheduledDockDoorPolicyService {
    return ScheduledDockDoorPolicyService(
        store = store,
        validator = defaultScheduledDockDoorPolicyValidator(),
        clock = clock,
        journal = mutableListOf<String>(),
    )
}
