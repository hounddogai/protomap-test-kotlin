package com.acme.transport.freightpay.audit.claim

import com.acme.transport.freightpay.clock.Clock

/** Moves audit checkpoints through their life cycle and keeps a journal of what it did. */
data class AuditCheckpointManager(
    var store: AuditCheckpointStore,
    var validator: AuditCheckpointValidator,
    var clock: Clock,
    var journal: MutableList<String>,
    var engine: AuditCheckpointEngine,
) {
    private fun note(message: String) {
        journal.add("${clock.now()} $message")
    }

    fun open(code: String) {
        val item = store.find(code) ?: error("audit checkpoint $code not found")
        validator.requireValid(item)
        item.moveTo(AuditCheckpointStatus.WARMING)
        store.save(item)
        note("$code is warming")
    }

    fun close(code: String, reason: String) {
        val item = store.find(code) ?: error("audit checkpoint $code not found")
        if (item.isTerminal()) {
            note("$code already closed")
            return
        }
        item.moveTo(AuditCheckpointStatus.STOPPED)
        store.save(item)
        note("$code is stopped: $reason")
    }

    fun evaluate(code: String): Double {
        val item = store.find(code) ?: error("audit checkpoint $code not found")
        val result = engine.price(item)
        note("$code evaluated at $result")
        return result
    }

    fun sweep(cutoff: Int): Int {
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

fun newAuditCheckpointManager(store: AuditCheckpointStore, clock: Clock): AuditCheckpointManager {
    return AuditCheckpointManager(
        store = store,
        validator = defaultAuditCheckpointValidator(),
        clock = clock,
        journal = mutableListOf<String>(),
        engine = defaultAuditCheckpointEngine(),
    )
}
