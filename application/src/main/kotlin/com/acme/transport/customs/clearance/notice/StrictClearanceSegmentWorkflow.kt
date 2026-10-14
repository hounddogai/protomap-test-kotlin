package com.acme.transport.customs.clearance.notice

import com.acme.transport.customs.clock.Clock
import com.acme.transport.kit.flags.Registry

/** Moves strict clearance segments through their life cycle and keeps a journal of what it did. */
data class StrictClearanceSegmentManager(
    var store: StrictClearanceSegmentStore,
    var validator: StrictClearanceSegmentValidator,
    var clock: Clock,
    var journal: MutableList<String>,
    var engine: StrictClearanceSegmentEngine,
) {
    private fun note(message: String) {
        journal.add("${clock.now()} $message")
    }

    fun start(code: String) {
        val item = store.find(code) ?: error("strict clearance segment $code not found")
        validator.requireValid(item)
        item.moveTo(StrictClearanceSegmentStatus.WARMING)
        store.save(item)
        note("$code is warming")
    }

    fun close(code: String, reason: String) {
        val item = store.find(code) ?: error("strict clearance segment $code not found")
        if (item.isTerminal()) {
            note("$code already closed")
            return
        }
        item.moveTo(StrictClearanceSegmentStatus.STOPPED)
        store.save(item)
        note("$code is stopped: $reason")
    }

    fun score(code: String): Double {
        val item = store.find(code) ?: error("strict clearance segment $code not found")
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

fun newStrictClearanceSegmentManager(store: StrictClearanceSegmentStore, clock: Clock, flags: Registry): StrictClearanceSegmentManager {
    return StrictClearanceSegmentManager(
        store = store,
        validator = defaultStrictClearanceSegmentValidator(),
        clock = clock,
        journal = mutableListOf<String>(),
        engine = defaultStrictClearanceSegmentEngine(flags),
    )
}
