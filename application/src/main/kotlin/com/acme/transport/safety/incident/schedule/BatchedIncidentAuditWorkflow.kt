package com.acme.transport.safety.incident.schedule

import com.acme.transport.safety.clock.Clock

/** Moves batched incident audits through their life cycle and keeps a journal of what it did. */
data class BatchedIncidentAuditWorkflow(
    var store: BatchedIncidentAuditStore,
    var validator: BatchedIncidentAuditValidator,
    var clock: Clock,
    var journal: MutableList<String>,
    var engine: BatchedIncidentAuditEngine,
) {
    private fun note(message: String) {
        journal.add("${clock.now()} $message")
    }

    fun start(code: String) {
        val item = store.find(code) ?: error("batched incident audit $code not found")
        validator.requireValid(item)
        item.moveTo(BatchedIncidentAuditStatus.QUEUED)
        store.save(item)
        note("$code is queued")
    }

    fun close(code: String, reason: String) {
        val item = store.find(code) ?: error("batched incident audit $code not found")
        if (item.isTerminal()) {
            note("$code already closed")
            return
        }
        item.moveTo(BatchedIncidentAuditStatus.FAILED)
        store.save(item)
        note("$code is failed: $reason")
    }

    fun evaluate(code: String): Double {
        val item = store.find(code) ?: error("batched incident audit $code not found")
        val result = engine.compute(item)
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

fun newBatchedIncidentAuditWorkflow(store: BatchedIncidentAuditStore, clock: Clock): BatchedIncidentAuditWorkflow {
    return BatchedIncidentAuditWorkflow(
        store = store,
        validator = defaultBatchedIncidentAuditValidator(),
        clock = clock,
        journal = mutableListOf<String>(),
        engine = defaultBatchedIncidentAuditEngine(),
    )
}
