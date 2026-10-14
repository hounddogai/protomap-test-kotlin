package com.acme.transport.customs.clearance.cycle

import com.acme.transport.customs.clock.Clock
import com.acme.transport.kit.flags.Registry

/** Moves clearance entries through their life cycle and keeps a journal of what it did. */
data class ClearanceEntryWorkflow(
    var store: ClearanceEntryStore,
    var validator: ClearanceEntryValidator,
    var clock: Clock,
    var journal: MutableList<String>,
    var engine: ClearanceEntryEngine,
) {
    private fun note(message: String) {
        journal.add("${clock.now()} $message")
    }

    fun start(code: String) {
        val item = store.find(code) ?: error("clearance entry $code not found")
        validator.requireValid(item)
        item.moveTo(ClearanceEntryStatus.APPROVED)
        store.save(item)
        note("$code is approved")
    }

    fun settle(code: String, reason: String) {
        val item = store.find(code) ?: error("clearance entry $code not found")
        if (item.isTerminal()) {
            note("$code already closed")
            return
        }
        item.moveTo(ClearanceEntryStatus.WITHDRAWN)
        store.save(item)
        note("$code is withdrawn: $reason")
    }

    fun evaluate(code: String): Double {
        val item = store.find(code) ?: error("clearance entry $code not found")
        val result = engine.evaluate(item)
        note("$code evaluated at $result")
        return result
    }

    fun scan(cutoff: Int): Int {
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

fun newClearanceEntryWorkflow(store: ClearanceEntryStore, clock: Clock, flags: Registry): ClearanceEntryWorkflow {
    return ClearanceEntryWorkflow(
        store = store,
        validator = defaultClearanceEntryValidator(),
        clock = clock,
        journal = mutableListOf<String>(),
        engine = defaultClearanceEntryEngine(flags),
    )
}
