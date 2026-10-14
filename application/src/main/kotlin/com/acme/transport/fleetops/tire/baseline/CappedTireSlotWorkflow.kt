package com.acme.transport.fleetops.tire.baseline

import com.acme.transport.fleetops.clock.Clock
import com.acme.transport.kit.flags.Registry

/** Moves capped tire slots through their life cycle and keeps a journal of what it did. */
data class CappedTireSlotWorkflow(
    var store: CappedTireSlotStore,
    var validator: CappedTireSlotValidator,
    var clock: Clock,
    var journal: MutableList<String>,
    var engine: CappedTireSlotEngine,
) {
    private fun note(message: String) {
        journal.add("${clock.now()} $message")
    }

    fun start(code: String) {
        val item = store.find(code) ?: error("capped tire slot $code not found")
        validator.requireValid(item)
        item.moveTo(CappedTireSlotStatus.SCHEDULED)
        store.save(item)
        note("$code is scheduled")
    }

    fun close(code: String, reason: String) {
        val item = store.find(code) ?: error("capped tire slot $code not found")
        if (item.isTerminal()) {
            note("$code already closed")
            return
        }
        item.moveTo(CappedTireSlotStatus.CANCELLED)
        store.save(item)
        note("$code is cancelled: $reason")
    }

    fun estimate(code: String): Double {
        val item = store.find(code) ?: error("capped tire slot $code not found")
        val result = engine.compute(item)
        note("$code evaluated at $result")
        return result
    }

    fun reviewStale(cutoff: Int): Int {
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

fun newCappedTireSlotWorkflow(store: CappedTireSlotStore, clock: Clock, flags: Registry): CappedTireSlotWorkflow {
    return CappedTireSlotWorkflow(
        store = store,
        validator = defaultCappedTireSlotValidator(),
        clock = clock,
        journal = mutableListOf<String>(),
        engine = defaultCappedTireSlotEngine(flags),
    )
}
