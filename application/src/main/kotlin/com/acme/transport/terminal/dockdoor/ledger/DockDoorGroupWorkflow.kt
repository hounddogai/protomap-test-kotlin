package com.acme.transport.terminal.dockdoor.ledger

import com.acme.transport.terminal.clock.Clock

/** Moves dock door groups through their life cycle and keeps a journal of what it did. */
data class DockDoorGroupService(
    var store: DockDoorGroupStore,
    var validator: DockDoorGroupValidator,
    var clock: Clock,
    var journal: MutableList<String>,
) {
    private fun note(message: String) {
        journal.add("${clock.now()} $message")
    }

    fun advance(code: String) {
        val item = store.find(code) ?: error("dock door group $code not found")
        validator.requireValid(item)
        item.moveTo(DockDoorGroupStatus.GRANTED)
        store.save(item)
        note("$code is granted")
    }

    fun settle(code: String, reason: String) {
        val item = store.find(code) ?: error("dock door group $code not found")
        if (item.isTerminal()) {
            note("$code already closed")
            return
        }
        item.moveTo(DockDoorGroupStatus.EXPIRED)
        store.save(item)
        note("$code is expired: $reason")
    }

    fun audit(cutoff: Int): Int {
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

fun newDockDoorGroupService(store: DockDoorGroupStore, clock: Clock): DockDoorGroupService {
    return DockDoorGroupService(
        store = store,
        validator = defaultDockDoorGroupValidator(),
        clock = clock,
        journal = mutableListOf<String>(),
    )
}
