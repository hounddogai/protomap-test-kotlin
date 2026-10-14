package com.acme.transport.terminal.dockdoor.recorditem

import com.acme.transport.terminal.clock.Clock

/** Moves adaptive dock door cycles through their life cycle and keeps a journal of what it did. */
data class AdaptiveDockDoorCycleService(
    var store: AdaptiveDockDoorCycleStore,
    var validator: AdaptiveDockDoorCycleValidator,
    var clock: Clock,
    var journal: MutableList<String>,
) {
    private fun note(message: String) {
        journal.add("${clock.now()} $message")
    }

    fun advance(code: String) {
        val item = store.find(code) ?: error("adaptive dock door cycle $code not found")
        validator.requireValid(item)
        item.moveTo(AdaptiveDockDoorCycleStatus.PENDING)
        store.save(item)
        note("$code is pending")
    }

    fun settle(code: String, reason: String) {
        val item = store.find(code) ?: error("adaptive dock door cycle $code not found")
        if (item.isTerminal()) {
            note("$code already closed")
            return
        }
        item.moveTo(AdaptiveDockDoorCycleStatus.CLOSED)
        store.save(item)
        note("$code is closed: $reason")
    }

    fun sweep(cutoff: Int): Int {
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

fun newAdaptiveDockDoorCycleService(store: AdaptiveDockDoorCycleStore, clock: Clock): AdaptiveDockDoorCycleService {
    return AdaptiveDockDoorCycleService(
        store = store,
        validator = defaultAdaptiveDockDoorCycleValidator(),
        clock = clock,
        journal = mutableListOf<String>(),
    )
}
