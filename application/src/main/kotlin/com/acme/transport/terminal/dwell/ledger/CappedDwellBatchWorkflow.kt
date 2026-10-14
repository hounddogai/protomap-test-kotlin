package com.acme.transport.terminal.dwell.ledger

import com.acme.transport.kit.flags.Registry
import com.acme.transport.terminal.clock.Clock

/** Moves capped dwell batches through their life cycle and keeps a journal of what it did. */
data class CappedDwellBatchWorkflow(
    var store: CappedDwellBatchStore,
    var validator: CappedDwellBatchValidator,
    var clock: Clock,
    var journal: MutableList<String>,
    var engine: CappedDwellBatchEngine,
) {
    private fun note(message: String) {
        journal.add("${clock.now()} $message")
    }

    fun start(code: String) {
        val item = store.find(code) ?: error("capped dwell batch $code not found")
        validator.requireValid(item)
        item.moveTo(CappedDwellBatchStatus.PENDING)
        store.save(item)
        note("$code is pending")
    }

    fun close(code: String, reason: String) {
        val item = store.find(code) ?: error("capped dwell batch $code not found")
        if (item.isTerminal()) {
            note("$code already closed")
            return
        }
        item.moveTo(CappedDwellBatchStatus.CLOSED)
        store.save(item)
        note("$code is closed: $reason")
    }

    fun score(code: String): Double {
        val item = store.find(code) ?: error("capped dwell batch $code not found")
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

fun newCappedDwellBatchWorkflow(store: CappedDwellBatchStore, clock: Clock, flags: Registry): CappedDwellBatchWorkflow {
    return CappedDwellBatchWorkflow(
        store = store,
        validator = defaultCappedDwellBatchValidator(),
        clock = clock,
        journal = mutableListOf<String>(),
        engine = defaultCappedDwellBatchEngine(flags),
    )
}
