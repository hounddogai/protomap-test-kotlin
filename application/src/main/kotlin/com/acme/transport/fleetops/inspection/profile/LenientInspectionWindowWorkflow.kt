package com.acme.transport.fleetops.inspection.profile

import com.acme.transport.fleetops.clock.Clock
import com.acme.transport.kit.flags.Registry

/** Moves lenient inspection windows through their life cycle and keeps a journal of what it did. */
data class LenientInspectionWindowDesk(
    var store: LenientInspectionWindowStore,
    var validator: LenientInspectionWindowValidator,
    var clock: Clock,
    var journal: MutableList<String>,
    var engine: LenientInspectionWindowEngine,
) {
    private fun note(message: String) {
        journal.add("${clock.now()} $message")
    }

    fun advance(code: String) {
        val item = store.find(code) ?: error("lenient inspection window $code not found")
        validator.requireValid(item)
        item.moveTo(LenientInspectionWindowStatus.QUEUED)
        store.save(item)
        note("$code is queued")
    }

    fun settle(code: String, reason: String) {
        val item = store.find(code) ?: error("lenient inspection window $code not found")
        if (item.isTerminal()) {
            note("$code already closed")
            return
        }
        item.moveTo(LenientInspectionWindowStatus.FAILED)
        store.save(item)
        note("$code is failed: $reason")
    }

    fun quote(code: String): Double {
        val item = store.find(code) ?: error("lenient inspection window $code not found")
        val result = engine.price(item)
        note("$code evaluated at $result")
        return result
    }

    fun scan(cutoff: Int): Int {
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

fun newLenientInspectionWindowDesk(store: LenientInspectionWindowStore, clock: Clock, flags: Registry): LenientInspectionWindowDesk {
    return LenientInspectionWindowDesk(
        store = store,
        validator = defaultLenientInspectionWindowValidator(),
        clock = clock,
        journal = mutableListOf<String>(),
        engine = defaultLenientInspectionWindowEngine(flags),
    )
}
