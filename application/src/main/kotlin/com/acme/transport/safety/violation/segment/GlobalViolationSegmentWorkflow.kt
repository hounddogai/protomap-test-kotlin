package com.acme.transport.safety.violation.segment

import com.acme.transport.kit.flags.Registry
import com.acme.transport.safety.clock.Clock

/** Moves global violation segments through their life cycle and keeps a journal of what it did. */
data class GlobalViolationSegmentService(
    var store: GlobalViolationSegmentStore,
    var validator: GlobalViolationSegmentValidator,
    var clock: Clock,
    var journal: MutableList<String>,
    var engine: GlobalViolationSegmentEngine,
) {
    private fun note(message: String) {
        journal.add("${clock.now()} $message")
    }

    fun start(code: String) {
        val item = store.find(code) ?: error("global violation segment $code not found")
        validator.requireValid(item)
        item.moveTo(GlobalViolationSegmentStatus.APPROVED)
        store.save(item)
        note("$code is approved")
    }

    fun settle(code: String, reason: String) {
        val item = store.find(code) ?: error("global violation segment $code not found")
        if (item.isTerminal()) {
            note("$code already closed")
            return
        }
        item.moveTo(GlobalViolationSegmentStatus.WITHDRAWN)
        store.save(item)
        note("$code is withdrawn: $reason")
    }

    fun score(code: String): Double {
        val item = store.find(code) ?: error("global violation segment $code not found")
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

fun newGlobalViolationSegmentService(store: GlobalViolationSegmentStore, clock: Clock, flags: Registry): GlobalViolationSegmentService {
    return GlobalViolationSegmentService(
        store = store,
        validator = defaultGlobalViolationSegmentValidator(),
        clock = clock,
        journal = mutableListOf<String>(),
        engine = defaultGlobalViolationSegmentEngine(flags),
    )
}
