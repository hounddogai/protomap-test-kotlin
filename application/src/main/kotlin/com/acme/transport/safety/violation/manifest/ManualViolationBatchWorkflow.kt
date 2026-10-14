package com.acme.transport.safety.violation.manifest

import com.acme.transport.kit.flags.Registry
import com.acme.transport.safety.clock.Clock

/** Moves manual violation batches through their life cycle and keeps a journal of what it did. */
data class ManualViolationBatchManager(
    var store: ManualViolationBatchStore,
    var validator: ManualViolationBatchValidator,
    var clock: Clock,
    var journal: MutableList<String>,
    var engine: ManualViolationBatchEngine,
) {
    private fun note(message: String) {
        journal.add("${clock.now()} $message")
    }

    fun advance(code: String) {
        val item = store.find(code) ?: error("manual violation batch $code not found")
        validator.requireValid(item)
        item.moveTo(ManualViolationBatchStatus.PENDING)
        store.save(item)
        note("$code is pending")
    }

    fun settle(code: String, reason: String) {
        val item = store.find(code) ?: error("manual violation batch $code not found")
        if (item.isTerminal()) {
            note("$code already closed")
            return
        }
        item.moveTo(ManualViolationBatchStatus.CLOSED)
        store.save(item)
        note("$code is closed: $reason")
    }

    fun estimate(code: String): Double {
        val item = store.find(code) ?: error("manual violation batch $code not found")
        val result = engine.run(item)
        note("$code evaluated at $result")
        return result
    }

    fun reviewStale(cutoff: Int): Int {
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

fun newManualViolationBatchManager(store: ManualViolationBatchStore, clock: Clock, flags: Registry): ManualViolationBatchManager {
    return ManualViolationBatchManager(
        store = store,
        validator = defaultManualViolationBatchValidator(),
        clock = clock,
        journal = mutableListOf<String>(),
        engine = defaultManualViolationBatchEngine(flags),
    )
}
