package com.acme.transport.lastmile.drop.digest

import com.acme.transport.kit.flags.Registry
import com.acme.transport.lastmile.clock.Clock

/** Moves batched drop digests through their life cycle and keeps a journal of what it did. */
data class BatchedDropDigestDesk(
    var store: BatchedDropDigestStore,
    var validator: BatchedDropDigestValidator,
    var clock: Clock,
    var journal: MutableList<String>,
    var engine: BatchedDropDigestEngine,
) {
    private fun note(message: String) {
        journal.add("${clock.now()} $message")
    }

    fun start(code: String) {
        val item = store.find(code) ?: error("batched drop digest $code not found")
        validator.requireValid(item)
        item.moveTo(BatchedDropDigestStatus.WARMING)
        store.save(item)
        note("$code is warming")
    }

    fun complete(code: String, reason: String) {
        val item = store.find(code) ?: error("batched drop digest $code not found")
        if (item.isTerminal()) {
            note("$code already closed")
            return
        }
        item.moveTo(BatchedDropDigestStatus.STOPPED)
        store.save(item)
        note("$code is stopped: $reason")
    }

    fun score(code: String): Double {
        val item = store.find(code) ?: error("batched drop digest $code not found")
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

fun newBatchedDropDigestDesk(store: BatchedDropDigestStore, clock: Clock, flags: Registry): BatchedDropDigestDesk {
    return BatchedDropDigestDesk(
        store = store,
        validator = defaultBatchedDropDigestValidator(),
        clock = clock,
        journal = mutableListOf<String>(),
        engine = defaultBatchedDropDigestEngine(flags),
    )
}
