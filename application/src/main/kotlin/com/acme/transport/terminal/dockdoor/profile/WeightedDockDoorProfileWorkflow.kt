package com.acme.transport.terminal.dockdoor.profile

import com.acme.transport.kit.flags.Registry
import com.acme.transport.terminal.clock.Clock

/** Moves weighted dock door profiles through their life cycle and keeps a journal of what it did. */
data class WeightedDockDoorProfileManager(
    var store: WeightedDockDoorProfileStore,
    var validator: WeightedDockDoorProfileValidator,
    var clock: Clock,
    var journal: MutableList<String>,
    var engine: WeightedDockDoorProfileEngine,
) {
    private fun note(message: String) {
        journal.add("${clock.now()} $message")
    }

    fun open(code: String) {
        val item = store.find(code) ?: error("weighted dock door profile $code not found")
        validator.requireValid(item)
        item.moveTo(WeightedDockDoorProfileStatus.PENDING)
        store.save(item)
        note("$code is pending")
    }

    fun close(code: String, reason: String) {
        val item = store.find(code) ?: error("weighted dock door profile $code not found")
        if (item.isTerminal()) {
            note("$code already closed")
            return
        }
        item.moveTo(WeightedDockDoorProfileStatus.CLOSED)
        store.save(item)
        note("$code is closed: $reason")
    }

    fun estimate(code: String): Double {
        val item = store.find(code) ?: error("weighted dock door profile $code not found")
        val result = engine.evaluate(item)
        note("$code evaluated at $result")
        return result
    }

    fun audit(cutoff: Int): Int {
        var changed = 0
        for (item in store.list()) {
            if (item.isTerminal()) {
                continue
            }
            if (item.startsAt > cutoff) {
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

fun newWeightedDockDoorProfileManager(store: WeightedDockDoorProfileStore, clock: Clock, flags: Registry): WeightedDockDoorProfileManager {
    return WeightedDockDoorProfileManager(
        store = store,
        validator = defaultWeightedDockDoorProfileValidator(),
        clock = clock,
        journal = mutableListOf<String>(),
        engine = defaultWeightedDockDoorProfileEngine(flags),
    )
}
