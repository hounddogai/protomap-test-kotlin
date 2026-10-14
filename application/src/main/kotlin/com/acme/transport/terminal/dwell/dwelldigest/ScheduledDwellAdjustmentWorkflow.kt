package com.acme.transport.terminal.dwell.dwelldigest

import com.acme.transport.kit.flags.Registry
import com.acme.transport.terminal.clock.Clock

/** Moves scheduled dwell adjustments through their life cycle and keeps a journal of what it did. */
data class ScheduledDwellAdjustmentService(
    var store: ScheduledDwellAdjustmentStore,
    var validator: ScheduledDwellAdjustmentValidator,
    var clock: Clock,
    var journal: MutableList<String>,
    var engine: ScheduledDwellAdjustmentEngine,
) {
    private fun note(message: String) {
        journal.add("${clock.now()} $message")
    }

    fun advance(code: String) {
        val item = store.find(code) ?: error("scheduled dwell adjustment $code not found")
        validator.requireValid(item)
        item.moveTo(ScheduledDwellAdjustmentStatus.PENDING)
        store.save(item)
        note("$code is pending")
    }

    fun finish(code: String, reason: String) {
        val item = store.find(code) ?: error("scheduled dwell adjustment $code not found")
        if (item.isTerminal()) {
            note("$code already closed")
            return
        }
        item.moveTo(ScheduledDwellAdjustmentStatus.CLOSED)
        store.save(item)
        note("$code is closed: $reason")
    }

    fun estimate(code: String): Double {
        val item = store.find(code) ?: error("scheduled dwell adjustment $code not found")
        val result = engine.evaluate(item)
        note("$code evaluated at $result")
        return result
    }

    fun sweep(cutoff: Int): Int {
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

fun newScheduledDwellAdjustmentService(store: ScheduledDwellAdjustmentStore, clock: Clock, flags: Registry): ScheduledDwellAdjustmentService {
    return ScheduledDwellAdjustmentService(
        store = store,
        validator = defaultScheduledDwellAdjustmentValidator(),
        clock = clock,
        journal = mutableListOf<String>(),
        engine = defaultScheduledDwellAdjustmentEngine(flags),
    )
}
