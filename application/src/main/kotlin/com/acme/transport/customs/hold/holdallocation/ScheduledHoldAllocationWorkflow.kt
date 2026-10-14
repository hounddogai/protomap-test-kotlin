package com.acme.transport.customs.hold.holdallocation

import com.acme.transport.customs.clock.Clock
import com.acme.transport.kit.flags.Registry

/** Moves scheduled hold allocations through their life cycle and keeps a journal of what it did. */
data class ScheduledHoldAllocationService(
    var store: ScheduledHoldAllocationStore,
    var validator: ScheduledHoldAllocationValidator,
    var clock: Clock,
    var journal: MutableList<String>,
    var engine: ScheduledHoldAllocationEngine,
) {
    private fun note(message: String) {
        journal.add("${clock.now()} $message")
    }

    fun open(code: String) {
        val item = store.find(code) ?: error("scheduled hold allocation $code not found")
        validator.requireValid(item)
        item.moveTo(ScheduledHoldAllocationStatus.IN_REVIEW)
        store.save(item)
        note("$code is in review")
    }

    fun complete(code: String, reason: String) {
        val item = store.find(code) ?: error("scheduled hold allocation $code not found")
        if (item.isTerminal()) {
            note("$code already closed")
            return
        }
        item.moveTo(ScheduledHoldAllocationStatus.ARCHIVED)
        store.save(item)
        note("$code is archived: $reason")
    }

    fun estimate(code: String): Double {
        val item = store.find(code) ?: error("scheduled hold allocation $code not found")
        val result = engine.evaluate(item)
        note("$code evaluated at $result")
        return result
    }

    fun reviewStale(cutoff: Int): Int {
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

fun newScheduledHoldAllocationService(store: ScheduledHoldAllocationStore, clock: Clock, flags: Registry): ScheduledHoldAllocationService {
    return ScheduledHoldAllocationService(
        store = store,
        validator = defaultScheduledHoldAllocationValidator(),
        clock = clock,
        journal = mutableListOf<String>(),
        engine = defaultScheduledHoldAllocationEngine(flags),
    )
}
