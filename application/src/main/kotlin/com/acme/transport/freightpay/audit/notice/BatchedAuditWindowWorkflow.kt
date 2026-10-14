package com.acme.transport.freightpay.audit.notice

import com.acme.transport.freightpay.clock.Clock
import com.acme.transport.kit.flags.Registry

/** Moves batched audit windows through their life cycle and keeps a journal of what it did. */
data class BatchedAuditWindowService(
    var store: BatchedAuditWindowStore,
    var validator: BatchedAuditWindowValidator,
    var clock: Clock,
    var journal: MutableList<String>,
    var engine: BatchedAuditWindowEngine,
) {
    private fun note(message: String) {
        journal.add("${clock.now()} $message")
    }

    fun open(code: String) {
        val item = store.find(code) ?: error("batched audit window $code not found")
        validator.requireValid(item)
        item.moveTo(BatchedAuditWindowStatus.QUEUED)
        store.save(item)
        note("$code is queued")
    }

    fun retire(code: String, reason: String) {
        val item = store.find(code) ?: error("batched audit window $code not found")
        if (item.isTerminal()) {
            note("$code already closed")
            return
        }
        item.moveTo(BatchedAuditWindowStatus.FAILED)
        store.save(item)
        note("$code is failed: $reason")
    }

    fun quote(code: String): Double {
        val item = store.find(code) ?: error("batched audit window $code not found")
        val result = engine.evaluate(item)
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

fun newBatchedAuditWindowService(store: BatchedAuditWindowStore, clock: Clock, flags: Registry): BatchedAuditWindowService {
    return BatchedAuditWindowService(
        store = store,
        validator = defaultBatchedAuditWindowValidator(),
        clock = clock,
        journal = mutableListOf<String>(),
        engine = defaultBatchedAuditWindowEngine(flags),
    )
}
