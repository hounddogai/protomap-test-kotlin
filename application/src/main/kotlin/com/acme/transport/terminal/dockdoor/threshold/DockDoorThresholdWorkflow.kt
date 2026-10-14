package com.acme.transport.terminal.dockdoor.threshold

import com.acme.transport.terminal.clock.Clock

/** Moves dock door thresholds through their life cycle and keeps a journal of what it did. */
data class DockDoorThresholdManager(
    var store: DockDoorThresholdStore,
    var validator: DockDoorThresholdValidator,
    var clock: Clock,
    var journal: MutableList<String>,
    var engine: DockDoorThresholdEngine,
) {
    private fun note(message: String) {
        journal.add("${clock.now()} $message")
    }

    fun open(code: String) {
        val item = store.find(code) ?: error("dock door threshold $code not found")
        validator.requireValid(item)
        item.moveTo(DockDoorThresholdStatus.PENDING)
        store.save(item)
        note("$code is pending")
    }

    fun close(code: String, reason: String) {
        val item = store.find(code) ?: error("dock door threshold $code not found")
        if (item.isTerminal()) {
            note("$code already closed")
            return
        }
        item.moveTo(DockDoorThresholdStatus.CLOSED)
        store.save(item)
        note("$code is closed: $reason")
    }

    fun evaluate(code: String): Double {
        val item = store.find(code) ?: error("dock door threshold $code not found")
        val result = engine.price(item)
        note("$code evaluated at $result")
        return result
    }

    fun sweep(cutoff: Int): Int {
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

fun newDockDoorThresholdManager(store: DockDoorThresholdStore, clock: Clock): DockDoorThresholdManager {
    return DockDoorThresholdManager(
        store = store,
        validator = defaultDockDoorThresholdValidator(),
        clock = clock,
        journal = mutableListOf<String>(),
        engine = defaultDockDoorThresholdEngine(),
    )
}
