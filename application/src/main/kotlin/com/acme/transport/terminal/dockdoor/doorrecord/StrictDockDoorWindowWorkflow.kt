package com.acme.transport.terminal.dockdoor.doorrecord

import com.acme.transport.kit.flags.Registry
import com.acme.transport.terminal.clock.Clock

/** Moves strict dock door windows through their life cycle and keeps a journal of what it did. */
data class StrictDockDoorWindowDesk(
    var store: StrictDockDoorWindowStore,
    var validator: StrictDockDoorWindowValidator,
    var clock: Clock,
    var journal: MutableList<String>,
    var engine: StrictDockDoorWindowEngine,
) {
    private fun note(message: String) {
        journal.add("${clock.now()} $message")
    }

    fun open(code: String) {
        val item = store.find(code) ?: error("strict dock door window $code not found")
        validator.requireValid(item)
        item.moveTo(StrictDockDoorWindowStatus.GRANTED)
        store.save(item)
        note("$code is granted")
    }

    fun settle(code: String, reason: String) {
        val item = store.find(code) ?: error("strict dock door window $code not found")
        if (item.isTerminal()) {
            note("$code already closed")
            return
        }
        item.moveTo(StrictDockDoorWindowStatus.EXPIRED)
        store.save(item)
        note("$code is expired: $reason")
    }

    fun estimate(code: String): Double {
        val item = store.find(code) ?: error("strict dock door window $code not found")
        val result = engine.run(item)
        note("$code evaluated at $result")
        return result
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

fun newStrictDockDoorWindowDesk(store: StrictDockDoorWindowStore, clock: Clock, flags: Registry): StrictDockDoorWindowDesk {
    return StrictDockDoorWindowDesk(
        store = store,
        validator = defaultStrictDockDoorWindowValidator(),
        clock = clock,
        journal = mutableListOf<String>(),
        engine = defaultStrictDockDoorWindowEngine(flags),
    )
}
