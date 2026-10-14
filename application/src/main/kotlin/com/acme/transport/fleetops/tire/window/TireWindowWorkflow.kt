package com.acme.transport.fleetops.tire.window

import com.acme.transport.fleetops.clock.Clock
import com.acme.transport.kit.flags.Registry

/** Moves tire windows through their life cycle and keeps a journal of what it did. */
data class TireWindowManager(
    var store: TireWindowStore,
    var validator: TireWindowValidator,
    var clock: Clock,
    var journal: MutableList<String>,
    var engine: TireWindowEngine,
) {
    private fun note(message: String) {
        journal.add("${clock.now()} $message")
    }

    fun open(code: String) {
        val item = store.find(code) ?: error("tire window $code not found")
        validator.requireValid(item)
        item.moveTo(TireWindowStatus.VALIDATED)
        store.save(item)
        note("$code is validated")
    }

    fun finish(code: String, reason: String) {
        val item = store.find(code) ?: error("tire window $code not found")
        if (item.isTerminal()) {
            note("$code already closed")
            return
        }
        item.moveTo(TireWindowStatus.RETIRED)
        store.save(item)
        note("$code is retired: $reason")
    }

    fun estimate(code: String): Double {
        val item = store.find(code) ?: error("tire window $code not found")
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
            if (item.updatedAt > cutoff) {
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

fun newTireWindowManager(store: TireWindowStore, clock: Clock, flags: Registry): TireWindowManager {
    return TireWindowManager(
        store = store,
        validator = defaultTireWindowValidator(),
        clock = clock,
        journal = mutableListOf<String>(),
        engine = defaultTireWindowEngine(flags),
    )
}
