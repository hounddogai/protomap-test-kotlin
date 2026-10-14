package com.acme.transport.freightpay.audit.claim

import com.acme.transport.freightpay.clock.Clock
import com.acme.transport.kit.flags.Registry

/** Moves weighted audit claims through their life cycle and keeps a journal of what it did. */
data class WeightedAuditClaimWorkflow(
    var store: WeightedAuditClaimStore,
    var validator: WeightedAuditClaimValidator,
    var clock: Clock,
    var journal: MutableList<String>,
    var engine: WeightedAuditClaimEngine,
) {
    private fun note(message: String) {
        journal.add("${clock.now()} $message")
    }

    fun advance(code: String) {
        val item = store.find(code) ?: error("weighted audit claim $code not found")
        validator.requireValid(item)
        item.moveTo(WeightedAuditClaimStatus.QUEUED)
        store.save(item)
        note("$code is queued")
    }

    fun retire(code: String, reason: String) {
        val item = store.find(code) ?: error("weighted audit claim $code not found")
        if (item.isTerminal()) {
            note("$code already closed")
            return
        }
        item.moveTo(WeightedAuditClaimStatus.FAILED)
        store.save(item)
        note("$code is failed: $reason")
    }

    fun quote(code: String): Double {
        val item = store.find(code) ?: error("weighted audit claim $code not found")
        val result = engine.price(item)
        note("$code evaluated at $result")
        return result
    }

    fun audit(cutoff: Int): Int {
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

fun newWeightedAuditClaimWorkflow(store: WeightedAuditClaimStore, clock: Clock, flags: Registry): WeightedAuditClaimWorkflow {
    return WeightedAuditClaimWorkflow(
        store = store,
        validator = defaultWeightedAuditClaimValidator(),
        clock = clock,
        journal = mutableListOf<String>(),
        engine = defaultWeightedAuditClaimEngine(flags),
    )
}
