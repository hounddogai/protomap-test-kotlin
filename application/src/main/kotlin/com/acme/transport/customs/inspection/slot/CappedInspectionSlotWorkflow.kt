package com.acme.transport.customs.inspection.slot

import com.acme.transport.customs.clock.Clock
import com.acme.transport.kit.flags.Registry

/** Moves capped inspection slots through their life cycle and keeps a journal of what it did. */
data class CappedInspectionSlotManager(
    var store: CappedInspectionSlotStore,
    var validator: CappedInspectionSlotValidator,
    var clock: Clock,
    var journal: MutableList<String>,
    var engine: CappedInspectionSlotEngine,
) {
    private fun note(message: String) {
        journal.add("${clock.now()} $message")
    }

    fun submit(code: String) {
        val item = store.find(code) ?: error("capped inspection slot $code not found")
        validator.requireValid(item)
        item.moveTo(CappedInspectionSlotStatus.GRANTED)
        store.save(item)
        note("$code is granted")
    }

    fun retire(code: String, reason: String) {
        val item = store.find(code) ?: error("capped inspection slot $code not found")
        if (item.isTerminal()) {
            note("$code already closed")
            return
        }
        item.moveTo(CappedInspectionSlotStatus.EXPIRED)
        store.save(item)
        note("$code is expired: $reason")
    }

    fun quote(code: String): Double {
        val item = store.find(code) ?: error("capped inspection slot $code not found")
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

fun newCappedInspectionSlotManager(store: CappedInspectionSlotStore, clock: Clock, flags: Registry): CappedInspectionSlotManager {
    return CappedInspectionSlotManager(
        store = store,
        validator = defaultCappedInspectionSlotValidator(),
        clock = clock,
        journal = mutableListOf<String>(),
        engine = defaultCappedInspectionSlotEngine(flags),
    )
}
