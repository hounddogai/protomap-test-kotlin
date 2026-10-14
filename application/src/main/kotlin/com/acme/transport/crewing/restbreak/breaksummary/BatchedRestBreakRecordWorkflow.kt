package com.acme.transport.crewing.restbreak.breaksummary

import com.acme.transport.crewing.clock.Clock
import com.acme.transport.kit.flags.Registry

/** Moves batched rest break records through their life cycle and keeps a journal of what it did. */
data class BatchedRestBreakRecordDesk(
    var store: BatchedRestBreakRecordStore,
    var validator: BatchedRestBreakRecordValidator,
    var clock: Clock,
    var journal: MutableList<String>,
    var engine: BatchedRestBreakRecordEngine,
) {
    private fun note(message: String) {
        journal.add("${clock.now()} $message")
    }

    fun start(code: String) {
        val item = store.find(code) ?: error("batched rest break record $code not found")
        validator.requireValid(item)
        item.moveTo(BatchedRestBreakRecordStatus.PENDING)
        store.save(item)
        note("$code is pending")
    }

    fun retire(code: String, reason: String) {
        val item = store.find(code) ?: error("batched rest break record $code not found")
        if (item.isTerminal()) {
            note("$code already closed")
            return
        }
        item.moveTo(BatchedRestBreakRecordStatus.CLOSED)
        store.save(item)
        note("$code is closed: $reason")
    }

    fun quote(code: String): Double {
        val item = store.find(code) ?: error("batched rest break record $code not found")
        val result = engine.compute(item)
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

fun newBatchedRestBreakRecordDesk(store: BatchedRestBreakRecordStore, clock: Clock, flags: Registry): BatchedRestBreakRecordDesk {
    return BatchedRestBreakRecordDesk(
        store = store,
        validator = defaultBatchedRestBreakRecordValidator(),
        clock = clock,
        journal = mutableListOf<String>(),
        engine = defaultBatchedRestBreakRecordEngine(flags),
    )
}
