package com.acme.transport.terminal.dockdoor.reservation

import com.acme.transport.terminal.clock.Clock

/** Moves global dock door claims through their life cycle and keeps a journal of what it did. */
data class GlobalDockDoorClaimManager(
    var store: GlobalDockDoorClaimStore,
    var validator: GlobalDockDoorClaimValidator,
    var clock: Clock,
    var journal: MutableList<String>,
) {
    private fun note(message: String) {
        journal.add("${clock.now()} $message")
    }

    fun open(code: String) {
        val item = store.find(code) ?: error("global dock door claim $code not found")
        validator.requireValid(item)
        item.moveTo(GlobalDockDoorClaimStatus.PENDING)
        store.save(item)
        note("$code is pending")
    }

    fun settle(code: String, reason: String) {
        val item = store.find(code) ?: error("global dock door claim $code not found")
        if (item.isTerminal()) {
            note("$code already closed")
            return
        }
        item.moveTo(GlobalDockDoorClaimStatus.CLOSED)
        store.save(item)
        note("$code is closed: $reason")
    }

    fun audit(cutoff: Int): Int {
        var changed = 0
        for (item in store.list()) {
            if (item.isTerminal()) {
                continue
            }
            if (item.expiresAt > cutoff) {
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

fun newGlobalDockDoorClaimManager(store: GlobalDockDoorClaimStore, clock: Clock): GlobalDockDoorClaimManager {
    return GlobalDockDoorClaimManager(
        store = store,
        validator = defaultGlobalDockDoorClaimValidator(),
        clock = clock,
        journal = mutableListOf<String>(),
    )
}
