package com.acme.transport.lastmile.drop.slot

import com.acme.transport.kit.flags.Registry
import com.acme.transport.lastmile.clock.Clock

/** Moves drop slots through their life cycle and keeps a journal of what it did. */
data class DropSlotDesk(
    var store: DropSlotStore,
    var validator: DropSlotValidator,
    var clock: Clock,
    var journal: MutableList<String>,
    var engine: DropSlotEngine,
) {
    private fun note(message: String) {
        journal.add("${clock.now()} $message")
    }

    fun submit(code: String) {
        val item = store.find(code) ?: error("drop slot $code not found")
        validator.requireValid(item)
        item.moveTo(DropSlotStatus.GRANTED)
        store.save(item)
        note("$code is granted")
    }

    fun finish(code: String, reason: String) {
        val item = store.find(code) ?: error("drop slot $code not found")
        if (item.isTerminal()) {
            note("$code already closed")
            return
        }
        item.moveTo(DropSlotStatus.EXPIRED)
        store.save(item)
        note("$code is expired: $reason")
    }

    fun quote(code: String): Double {
        val item = store.find(code) ?: error("drop slot $code not found")
        val result = engine.compute(item)
        note("$code evaluated at $result")
        return result
    }

    fun scan(cutoff: Int): Int {
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

fun newDropSlotDesk(store: DropSlotStore, clock: Clock, flags: Registry): DropSlotDesk {
    return DropSlotDesk(
        store = store,
        validator = defaultDropSlotValidator(),
        clock = clock,
        journal = mutableListOf<String>(),
        engine = defaultDropSlotEngine(flags),
    )
}
