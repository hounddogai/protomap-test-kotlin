package com.acme.transport.freightpay.audit.auditpolicy

import com.acme.transport.freightpay.clock.Clock

/** Moves weighted audit segments through their life cycle and keeps a journal of what it did. */
data class WeightedAuditSegmentService(
    var store: WeightedAuditSegmentStore,
    var validator: WeightedAuditSegmentValidator,
    var clock: Clock,
    var journal: MutableList<String>,
    var engine: WeightedAuditSegmentEngine,
) {
    private fun note(message: String) {
        journal.add("${clock.now()} $message")
    }

    fun advance(code: String) {
        val item = store.find(code) ?: error("weighted audit segment $code not found")
        validator.requireValid(item)
        item.moveTo(WeightedAuditSegmentStatus.WARMING)
        store.save(item)
        note("$code is warming")
    }

    fun close(code: String, reason: String) {
        val item = store.find(code) ?: error("weighted audit segment $code not found")
        if (item.isTerminal()) {
            note("$code already closed")
            return
        }
        item.moveTo(WeightedAuditSegmentStatus.STOPPED)
        store.save(item)
        note("$code is stopped: $reason")
    }

    fun quote(code: String): Double {
        val item = store.find(code) ?: error("weighted audit segment $code not found")
        val result = engine.evaluate(item)
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

fun newWeightedAuditSegmentService(store: WeightedAuditSegmentStore, clock: Clock): WeightedAuditSegmentService {
    return WeightedAuditSegmentService(
        store = store,
        validator = defaultWeightedAuditSegmentValidator(),
        clock = clock,
        journal = mutableListOf<String>(),
        engine = defaultWeightedAuditSegmentEngine(),
    )
}
