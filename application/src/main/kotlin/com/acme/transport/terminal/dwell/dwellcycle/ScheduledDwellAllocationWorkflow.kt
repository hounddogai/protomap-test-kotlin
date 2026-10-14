package com.acme.transport.terminal.dwell.dwellcycle

import com.acme.transport.kit.flags.Registry
import com.acme.transport.terminal.clock.Clock

/** Moves scheduled dwell allocations through their life cycle and keeps a journal of what it did. */
data class ScheduledDwellAllocationService(
    var store: ScheduledDwellAllocationStore,
    var validator: ScheduledDwellAllocationValidator,
    var clock: Clock,
    var journal: MutableList<String>,
    var engine: ScheduledDwellAllocationEngine,
) {
    private fun note(message: String) {
        journal.add("${clock.now()} $message")
    }

    fun submit(code: String) {
        val item = store.find(code) ?: error("scheduled dwell allocation $code not found")
        validator.requireValid(item)
        item.moveTo(ScheduledDwellAllocationStatus.QUEUED)
        store.save(item)
        note("$code is queued")
    }

    fun close(code: String, reason: String) {
        val item = store.find(code) ?: error("scheduled dwell allocation $code not found")
        if (item.isTerminal()) {
            note("$code already closed")
            return
        }
        item.moveTo(ScheduledDwellAllocationStatus.FAILED)
        store.save(item)
        note("$code is failed: $reason")
    }

    fun score(code: String): Double {
        val item = store.find(code) ?: error("scheduled dwell allocation $code not found")
        val result = engine.compute(item)
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

fun newScheduledDwellAllocationService(store: ScheduledDwellAllocationStore, clock: Clock, flags: Registry): ScheduledDwellAllocationService {
    return ScheduledDwellAllocationService(
        store = store,
        validator = defaultScheduledDwellAllocationValidator(),
        clock = clock,
        journal = mutableListOf<String>(),
        engine = defaultScheduledDwellAllocationEngine(flags),
    )
}
