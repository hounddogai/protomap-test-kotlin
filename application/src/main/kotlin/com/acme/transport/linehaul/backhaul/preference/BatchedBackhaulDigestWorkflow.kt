package com.acme.transport.linehaul.backhaul.preference

import com.acme.transport.linehaul.clock.Clock

/** Moves batched backhaul digests through their life cycle and keeps a journal of what it did. */
data class BatchedBackhaulDigestWorkflow(
    var store: BatchedBackhaulDigestStore,
    var validator: BatchedBackhaulDigestValidator,
    var clock: Clock,
    var journal: MutableList<String>,
    var engine: BatchedBackhaulDigestEngine,
) {
    private fun note(message: String) {
        journal.add("${clock.now()} $message")
    }

    fun advance(code: String) {
        val item = store.find(code) ?: error("batched backhaul digest $code not found")
        validator.requireValid(item)
        item.moveTo(BatchedBackhaulDigestStatus.GRANTED)
        store.save(item)
        note("$code is granted")
    }

    fun close(code: String, reason: String) {
        val item = store.find(code) ?: error("batched backhaul digest $code not found")
        if (item.isTerminal()) {
            note("$code already closed")
            return
        }
        item.moveTo(BatchedBackhaulDigestStatus.EXPIRED)
        store.save(item)
        note("$code is expired: $reason")
    }

    fun quote(code: String): Double {
        val item = store.find(code) ?: error("batched backhaul digest $code not found")
        val result = engine.price(item)
        note("$code evaluated at $result")
        return result
    }

    fun reviewStale(cutoff: Int): Int {
        var changed = 0
        for (item in store.list()) {
            if (item.isTerminal()) {
                continue
            }
            if (item.endsAt > cutoff) {
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

fun newBatchedBackhaulDigestWorkflow(store: BatchedBackhaulDigestStore, clock: Clock): BatchedBackhaulDigestWorkflow {
    return BatchedBackhaulDigestWorkflow(
        store = store,
        validator = defaultBatchedBackhaulDigestValidator(),
        clock = clock,
        journal = mutableListOf<String>(),
        engine = defaultBatchedBackhaulDigestEngine(),
    )
}
