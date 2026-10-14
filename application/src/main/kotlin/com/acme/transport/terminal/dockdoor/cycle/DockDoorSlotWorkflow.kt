package com.acme.transport.terminal.dockdoor.cycle

import com.acme.transport.kit.flags.Registry
import com.acme.transport.terminal.clock.Clock

/** Moves dock door slots through their life cycle and keeps a journal of what it did. */
data class DockDoorSlotService(
    var store: DockDoorSlotStore,
    var validator: DockDoorSlotValidator,
    var clock: Clock,
    var journal: MutableList<String>,
    var engine: DockDoorSlotEngine,
) {
    private fun note(message: String) {
        journal.add("${clock.now()} $message")
    }

    fun submit(code: String) {
        val item = store.find(code) ?: error("dock door slot $code not found")
        validator.requireValid(item)
        item.moveTo(DockDoorSlotStatus.APPROVED)
        store.save(item)
        note("$code is approved")
    }

    fun settle(code: String, reason: String) {
        val item = store.find(code) ?: error("dock door slot $code not found")
        if (item.isTerminal()) {
            note("$code already closed")
            return
        }
        item.moveTo(DockDoorSlotStatus.WITHDRAWN)
        store.save(item)
        note("$code is withdrawn: $reason")
    }

    fun estimate(code: String): Double {
        val item = store.find(code) ?: error("dock door slot $code not found")
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

fun newDockDoorSlotService(store: DockDoorSlotStore, clock: Clock, flags: Registry): DockDoorSlotService {
    return DockDoorSlotService(
        store = store,
        validator = defaultDockDoorSlotValidator(),
        clock = clock,
        journal = mutableListOf<String>(),
        engine = defaultDockDoorSlotEngine(flags),
    )
}
