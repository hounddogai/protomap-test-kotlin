package com.acme.transport.terminal.sortation.manifest

import com.acme.transport.kit.flags.Registry
import com.acme.transport.terminal.clock.Clock

/** Moves sortation slots through their life cycle and keeps a journal of what it did. */
data class SortationSlotManager(
    var store: SortationSlotStore,
    var validator: SortationSlotValidator,
    var clock: Clock,
    var journal: MutableList<String>,
    var engine: SortationSlotEngine,
) {
    private fun note(message: String) {
        journal.add("${clock.now()} $message")
    }

    fun submit(code: String) {
        val item = store.find(code) ?: error("sortation slot $code not found")
        validator.requireValid(item)
        item.moveTo(SortationSlotStatus.SCHEDULED)
        store.save(item)
        note("$code is scheduled")
    }

    fun retire(code: String, reason: String) {
        val item = store.find(code) ?: error("sortation slot $code not found")
        if (item.isTerminal()) {
            note("$code already closed")
            return
        }
        item.moveTo(SortationSlotStatus.CANCELLED)
        store.save(item)
        note("$code is cancelled: $reason")
    }

    fun estimate(code: String): Double {
        val item = store.find(code) ?: error("sortation slot $code not found")
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

fun newSortationSlotManager(store: SortationSlotStore, clock: Clock, flags: Registry): SortationSlotManager {
    return SortationSlotManager(
        store = store,
        validator = defaultSortationSlotValidator(),
        clock = clock,
        journal = mutableListOf<String>(),
        engine = defaultSortationSlotEngine(flags),
    )
}
