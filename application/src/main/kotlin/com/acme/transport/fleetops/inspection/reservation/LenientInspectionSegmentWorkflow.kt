package com.acme.transport.fleetops.inspection.reservation

import com.acme.transport.fleetops.clock.Clock
import com.acme.transport.kit.flags.Registry

/** Moves lenient inspection segments through their life cycle and keeps a journal of what it did. */
data class LenientInspectionSegmentWorkflow(
    var store: LenientInspectionSegmentStore,
    var validator: LenientInspectionSegmentValidator,
    var clock: Clock,
    var journal: MutableList<String>,
    var engine: LenientInspectionSegmentEngine,
) {
    private fun note(message: String) {
        journal.add("${clock.now()} $message")
    }

    fun advance(code: String) {
        val item = store.find(code) ?: error("lenient inspection segment $code not found")
        validator.requireValid(item)
        item.moveTo(LenientInspectionSegmentStatus.GRANTED)
        store.save(item)
        note("$code is granted")
    }

    fun settle(code: String, reason: String) {
        val item = store.find(code) ?: error("lenient inspection segment $code not found")
        if (item.isTerminal()) {
            note("$code already closed")
            return
        }
        item.moveTo(LenientInspectionSegmentStatus.EXPIRED)
        store.save(item)
        note("$code is expired: $reason")
    }

    fun score(code: String): Double {
        val item = store.find(code) ?: error("lenient inspection segment $code not found")
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

fun newLenientInspectionSegmentWorkflow(store: LenientInspectionSegmentStore, clock: Clock, flags: Registry): LenientInspectionSegmentWorkflow {
    return LenientInspectionSegmentWorkflow(
        store = store,
        validator = defaultLenientInspectionSegmentValidator(),
        clock = clock,
        journal = mutableListOf<String>(),
        engine = defaultLenientInspectionSegmentEngine(flags),
    )
}
