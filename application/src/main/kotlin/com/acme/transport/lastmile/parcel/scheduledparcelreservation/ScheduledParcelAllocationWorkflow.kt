package com.acme.transport.lastmile.parcel.scheduledparcelreservation

import com.acme.transport.kit.flags.Registry
import com.acme.transport.lastmile.clock.Clock

/** Moves scheduled parcel allocations through their life cycle and keeps a journal of what it did. */
data class ScheduledParcelAllocationCoordinator(
    var store: ScheduledParcelAllocationStore,
    var validator: ScheduledParcelAllocationValidator,
    var clock: Clock,
    var journal: MutableList<String>,
    var engine: ScheduledParcelAllocationEngine,
) {
    private fun note(message: String) {
        journal.add("${clock.now()} $message")
    }

    fun start(code: String) {
        val item = store.find(code) ?: error("scheduled parcel allocation $code not found")
        validator.requireValid(item)
        item.moveTo(ScheduledParcelAllocationStatus.PENDING)
        store.save(item)
        note("$code is pending")
    }

    fun complete(code: String, reason: String) {
        val item = store.find(code) ?: error("scheduled parcel allocation $code not found")
        if (item.isTerminal()) {
            note("$code already closed")
            return
        }
        item.moveTo(ScheduledParcelAllocationStatus.CLOSED)
        store.save(item)
        note("$code is closed: $reason")
    }

    fun evaluate(code: String): Double {
        val item = store.find(code) ?: error("scheduled parcel allocation $code not found")
        val result = engine.compute(item)
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

fun newScheduledParcelAllocationCoordinator(store: ScheduledParcelAllocationStore, clock: Clock, flags: Registry): ScheduledParcelAllocationCoordinator {
    return ScheduledParcelAllocationCoordinator(
        store = store,
        validator = defaultScheduledParcelAllocationValidator(),
        clock = clock,
        journal = mutableListOf<String>(),
        engine = defaultScheduledParcelAllocationEngine(flags),
    )
}
