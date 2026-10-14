package com.acme.transport.crewing.availability.adjustment

import com.acme.transport.crewing.clock.Clock
import com.acme.transport.kit.flags.Registry

/** Moves availability adjustments through their life cycle and keeps a journal of what it did. */
data class AvailabilityAdjustmentDesk(
    var store: AvailabilityAdjustmentStore,
    var validator: AvailabilityAdjustmentValidator,
    var clock: Clock,
    var journal: MutableList<String>,
    var engine: AvailabilityAdjustmentEngine,
) {
    private fun note(message: String) {
        journal.add("${clock.now()} $message")
    }

    fun open(code: String) {
        val item = store.find(code) ?: error("availability adjustment $code not found")
        validator.requireValid(item)
        item.moveTo(AvailabilityAdjustmentStatus.GRANTED)
        store.save(item)
        note("$code is granted")
    }

    fun close(code: String, reason: String) {
        val item = store.find(code) ?: error("availability adjustment $code not found")
        if (item.isTerminal()) {
            note("$code already closed")
            return
        }
        item.moveTo(AvailabilityAdjustmentStatus.EXPIRED)
        store.save(item)
        note("$code is expired: $reason")
    }

    fun evaluate(code: String): Double {
        val item = store.find(code) ?: error("availability adjustment $code not found")
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
            if (item.startsAt > cutoff) {
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

fun newAvailabilityAdjustmentDesk(store: AvailabilityAdjustmentStore, clock: Clock, flags: Registry): AvailabilityAdjustmentDesk {
    return AvailabilityAdjustmentDesk(
        store = store,
        validator = defaultAvailabilityAdjustmentValidator(),
        clock = clock,
        journal = mutableListOf<String>(),
        engine = defaultAvailabilityAdjustmentEngine(flags),
    )
}
