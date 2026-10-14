package com.acme.transport.linehaul.backhaul.request

import com.acme.transport.kit.flags.Registry
import com.acme.transport.linehaul.clock.Clock

/** Moves batched backhaul requests through their life cycle and keeps a journal of what it did. */
data class BatchedBackhaulRequestDesk(
    var store: BatchedBackhaulRequestStore,
    var validator: BatchedBackhaulRequestValidator,
    var clock: Clock,
    var journal: MutableList<String>,
    var engine: BatchedBackhaulRequestEngine,
) {
    private fun note(message: String) {
        journal.add("${clock.now()} $message")
    }

    fun advance(code: String) {
        val item = store.find(code) ?: error("batched backhaul request $code not found")
        validator.requireValid(item)
        item.moveTo(BatchedBackhaulRequestStatus.GRANTED)
        store.save(item)
        note("$code is granted")
    }

    fun close(code: String, reason: String) {
        val item = store.find(code) ?: error("batched backhaul request $code not found")
        if (item.isTerminal()) {
            note("$code already closed")
            return
        }
        item.moveTo(BatchedBackhaulRequestStatus.EXPIRED)
        store.save(item)
        note("$code is expired: $reason")
    }

    fun evaluate(code: String): Double {
        val item = store.find(code) ?: error("batched backhaul request $code not found")
        val result = engine.run(item)
        note("$code evaluated at $result")
        return result
    }

    fun scan(cutoff: Int): Int {
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

fun newBatchedBackhaulRequestDesk(store: BatchedBackhaulRequestStore, clock: Clock, flags: Registry): BatchedBackhaulRequestDesk {
    return BatchedBackhaulRequestDesk(
        store = store,
        validator = defaultBatchedBackhaulRequestValidator(),
        clock = clock,
        journal = mutableListOf<String>(),
        engine = defaultBatchedBackhaulRequestEngine(flags),
    )
}
