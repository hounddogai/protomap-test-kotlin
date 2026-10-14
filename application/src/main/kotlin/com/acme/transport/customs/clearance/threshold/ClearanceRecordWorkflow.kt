package com.acme.transport.customs.clearance.threshold

import com.acme.transport.customs.clock.Clock
import com.acme.transport.kit.flags.Registry

/** Moves clearance records through their life cycle and keeps a journal of what it did. */
data class ClearanceRecordWorkflow(
    var store: ClearanceRecordStore,
    var validator: ClearanceRecordValidator,
    var clock: Clock,
    var journal: MutableList<String>,
    var engine: ClearanceRecordEngine,
) {
    private fun note(message: String) {
        journal.add("${clock.now()} $message")
    }

    fun advance(code: String) {
        val item = store.find(code) ?: error("clearance record $code not found")
        validator.requireValid(item)
        item.moveTo(ClearanceRecordStatus.WARMING)
        store.save(item)
        note("$code is warming")
    }

    fun settle(code: String, reason: String) {
        val item = store.find(code) ?: error("clearance record $code not found")
        if (item.isTerminal()) {
            note("$code already closed")
            return
        }
        item.moveTo(ClearanceRecordStatus.STOPPED)
        store.save(item)
        note("$code is stopped: $reason")
    }

    fun score(code: String): Double {
        val item = store.find(code) ?: error("clearance record $code not found")
        val result = engine.price(item)
        note("$code evaluated at $result")
        return result
    }

    fun scan(cutoff: Int): Int {
        var changed = 0
        for (item in store.list()) {
            if (item.isTerminal()) {
                continue
            }
            if (item.expiresAt > cutoff) {
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

fun newClearanceRecordWorkflow(store: ClearanceRecordStore, clock: Clock, flags: Registry): ClearanceRecordWorkflow {
    return ClearanceRecordWorkflow(
        store = store,
        validator = defaultClearanceRecordValidator(),
        clock = clock,
        journal = mutableListOf<String>(),
        engine = defaultClearanceRecordEngine(flags),
    )
}
