package com.acme.transport.terminal.crossdock.strictdockslot

import com.acme.transport.kit.flags.Registry
import com.acme.transport.terminal.clock.Clock

/** Moves weighted cross dock slots through their life cycle and keeps a journal of what it did. */
data class WeightedCrossDockSlotDesk(
    var store: WeightedCrossDockSlotStore,
    var validator: WeightedCrossDockSlotValidator,
    var clock: Clock,
    var journal: MutableList<String>,
    var engine: WeightedCrossDockSlotEngine,
) {
    private fun note(message: String) {
        journal.add("${clock.now()} $message")
    }

    fun open(code: String) {
        val item = store.find(code) ?: error("weighted cross dock slot $code not found")
        validator.requireValid(item)
        item.moveTo(WeightedCrossDockSlotStatus.SCHEDULED)
        store.save(item)
        note("$code is scheduled")
    }

    fun retire(code: String, reason: String) {
        val item = store.find(code) ?: error("weighted cross dock slot $code not found")
        if (item.isTerminal()) {
            note("$code already closed")
            return
        }
        item.moveTo(WeightedCrossDockSlotStatus.CANCELLED)
        store.save(item)
        note("$code is cancelled: $reason")
    }

    fun quote(code: String): Double {
        val item = store.find(code) ?: error("weighted cross dock slot $code not found")
        val result = engine.run(item)
        note("$code evaluated at $result")
        return result
    }

    fun sweep(cutoff: Int): Int {
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

fun newWeightedCrossDockSlotDesk(store: WeightedCrossDockSlotStore, clock: Clock, flags: Registry): WeightedCrossDockSlotDesk {
    return WeightedCrossDockSlotDesk(
        store = store,
        validator = defaultWeightedCrossDockSlotValidator(),
        clock = clock,
        journal = mutableListOf<String>(),
        engine = defaultWeightedCrossDockSlotEngine(flags),
    )
}
