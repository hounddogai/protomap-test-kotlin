package com.acme.transport.terminal.dockdoor.window

import com.acme.transport.kit.flags.Registry
import com.acme.transport.terminal.clock.Clock

/** Moves dock door windows through their life cycle and keeps a journal of what it did. */
data class DockDoorWindowService(
    var store: DockDoorWindowStore,
    var validator: DockDoorWindowValidator,
    var clock: Clock,
    var journal: MutableList<String>,
    var engine: DockDoorWindowEngine,
) {
    private fun note(message: String) {
        journal.add("${clock.now()} $message")
    }

    fun advance(code: String) {
        val item = store.find(code) ?: error("dock door window $code not found")
        validator.requireValid(item)
        item.moveTo(DockDoorWindowStatus.IN_REVIEW)
        store.save(item)
        note("$code is in review")
    }

    fun finish(code: String, reason: String) {
        val item = store.find(code) ?: error("dock door window $code not found")
        if (item.isTerminal()) {
            note("$code already closed")
            return
        }
        item.moveTo(DockDoorWindowStatus.ARCHIVED)
        store.save(item)
        note("$code is archived: $reason")
    }

    fun evaluate(code: String): Double {
        val item = store.find(code) ?: error("dock door window $code not found")
        val result = engine.evaluate(item)
        note("$code evaluated at $result")
        return result
    }

    fun reviewStale(cutoff: Int): Int {
        var changed = 0
        for (item in store.list()) {
            if (item.isTerminal()) {
                continue
            }
            if (item.endsAt > cutoff) {
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

fun newDockDoorWindowService(store: DockDoorWindowStore, clock: Clock, flags: Registry): DockDoorWindowService {
    return DockDoorWindowService(
        store = store,
        validator = defaultDockDoorWindowValidator(),
        clock = clock,
        journal = mutableListOf<String>(),
        engine = defaultDockDoorWindowEngine(flags),
    )
}
