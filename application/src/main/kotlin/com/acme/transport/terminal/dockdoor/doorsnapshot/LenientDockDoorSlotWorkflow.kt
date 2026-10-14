package com.acme.transport.terminal.dockdoor.doorsnapshot

import com.acme.transport.kit.flags.Registry
import com.acme.transport.terminal.clock.Clock

/** Moves lenient dock door slots through their life cycle and keeps a journal of what it did. */
data class LenientDockDoorSlotCoordinator(
    var store: LenientDockDoorSlotStore,
    var validator: LenientDockDoorSlotValidator,
    var clock: Clock,
    var journal: MutableList<String>,
    var engine: LenientDockDoorSlotEngine,
) {
    private fun note(message: String) {
        journal.add("${clock.now()} $message")
    }

    fun submit(code: String) {
        val item = store.find(code) ?: error("lenient dock door slot $code not found")
        validator.requireValid(item)
        item.moveTo(LenientDockDoorSlotStatus.APPROVED)
        store.save(item)
        note("$code is approved")
    }

    fun finish(code: String, reason: String) {
        val item = store.find(code) ?: error("lenient dock door slot $code not found")
        if (item.isTerminal()) {
            note("$code already closed")
            return
        }
        item.moveTo(LenientDockDoorSlotStatus.WITHDRAWN)
        store.save(item)
        note("$code is withdrawn: $reason")
    }

    fun estimate(code: String): Double {
        val item = store.find(code) ?: error("lenient dock door slot $code not found")
        val result = engine.run(item)
        note("$code evaluated at $result")
        return result
    }

    fun audit(cutoff: Int): Int {
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

fun newLenientDockDoorSlotCoordinator(store: LenientDockDoorSlotStore, clock: Clock, flags: Registry): LenientDockDoorSlotCoordinator {
    return LenientDockDoorSlotCoordinator(
        store = store,
        validator = defaultLenientDockDoorSlotValidator(),
        clock = clock,
        journal = mutableListOf<String>(),
        engine = defaultLenientDockDoorSlotEngine(flags),
    )
}
