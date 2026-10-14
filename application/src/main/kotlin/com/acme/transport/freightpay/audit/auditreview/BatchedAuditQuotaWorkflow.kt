package com.acme.transport.freightpay.audit.auditreview

import com.acme.transport.freightpay.clock.Clock
import com.acme.transport.kit.flags.Registry

/** Moves batched audit quotas through their life cycle and keeps a journal of what it did. */
data class BatchedAuditQuotaDesk(
    var store: BatchedAuditQuotaStore,
    var validator: BatchedAuditQuotaValidator,
    var clock: Clock,
    var journal: MutableList<String>,
    var engine: BatchedAuditQuotaEngine,
) {
    private fun note(message: String) {
        journal.add("${clock.now()} $message")
    }

    fun advance(code: String) {
        val item = store.find(code) ?: error("batched audit quota $code not found")
        validator.requireValid(item)
        item.moveTo(BatchedAuditQuotaStatus.QUEUED)
        store.save(item)
        note("$code is queued")
    }

    fun complete(code: String, reason: String) {
        val item = store.find(code) ?: error("batched audit quota $code not found")
        if (item.isTerminal()) {
            note("$code already closed")
            return
        }
        item.moveTo(BatchedAuditQuotaStatus.FAILED)
        store.save(item)
        note("$code is failed: $reason")
    }

    fun estimate(code: String): Double {
        val item = store.find(code) ?: error("batched audit quota $code not found")
        val result = engine.run(item)
        note("$code evaluated at $result")
        return result
    }

    fun sweep(cutoff: Int): Int {
        var changed = 0
        for (item in store.list()) {
            if (item.isTerminal()) {
                continue
            }
            if (item.createdAt > cutoff) {
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

fun newBatchedAuditQuotaDesk(store: BatchedAuditQuotaStore, clock: Clock, flags: Registry): BatchedAuditQuotaDesk {
    return BatchedAuditQuotaDesk(
        store = store,
        validator = defaultBatchedAuditQuotaValidator(),
        clock = clock,
        journal = mutableListOf<String>(),
        engine = defaultBatchedAuditQuotaEngine(flags),
    )
}
