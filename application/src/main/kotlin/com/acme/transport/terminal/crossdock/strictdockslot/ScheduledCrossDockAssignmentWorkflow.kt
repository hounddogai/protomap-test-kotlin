package com.acme.transport.terminal.crossdock.strictdockslot

import com.acme.transport.kit.flags.Registry
import com.acme.transport.terminal.clock.Clock

/**
 * Moves scheduled cross dock assignments through their life cycle and keeps a journal of what it did.
 */
data class ScheduledCrossDockAssignmentDesk(
    var store: ScheduledCrossDockAssignmentStore,
    var validator: ScheduledCrossDockAssignmentValidator,
    var clock: Clock,
    var journal: MutableList<String>,
    var engine: ScheduledCrossDockAssignmentEngine,
) {
    private fun note(message: String) {
        journal.add("${clock.now()} $message")
    }

    fun submit(code: String) {
        val item = store.find(code) ?: error("scheduled cross dock assignment $code not found")
        validator.requireValid(item)
        item.moveTo(ScheduledCrossDockAssignmentStatus.GRANTED)
        store.save(item)
        note("$code is granted")
    }

    fun retire(code: String, reason: String) {
        val item = store.find(code) ?: error("scheduled cross dock assignment $code not found")
        if (item.isTerminal()) {
            note("$code already closed")
            return
        }
        item.moveTo(ScheduledCrossDockAssignmentStatus.EXPIRED)
        store.save(item)
        note("$code is expired: $reason")
    }

    fun evaluate(code: String): Double {
        val item = store.find(code) ?: error("scheduled cross dock assignment $code not found")
        val result = engine.price(item)
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

fun newScheduledCrossDockAssignmentDesk(store: ScheduledCrossDockAssignmentStore, clock: Clock, flags: Registry): ScheduledCrossDockAssignmentDesk {
    return ScheduledCrossDockAssignmentDesk(
        store = store,
        validator = defaultScheduledCrossDockAssignmentValidator(),
        clock = clock,
        journal = mutableListOf<String>(),
        engine = defaultScheduledCrossDockAssignmentEngine(flags),
    )
}
