package com.acme.transport.freightpay.dispute.adjustment

import com.acme.transport.freightpay.clock.Clock
import com.acme.transport.kit.flags.Registry

/** Moves dispute thresholds through their life cycle and keeps a journal of what it did. */
data class DisputeThresholdWorkflow(
    var store: DisputeThresholdStore,
    var validator: DisputeThresholdValidator,
    var clock: Clock,
    var journal: MutableList<String>,
    var engine: DisputeThresholdEngine,
) {
    private fun note(message: String) {
        journal.add("${clock.now()} $message")
    }

    fun open(code: String) {
        val item = store.find(code) ?: error("dispute threshold $code not found")
        validator.requireValid(item)
        item.moveTo(DisputeThresholdStatus.IN_REVIEW)
        store.save(item)
        note("$code is in review")
    }

    fun settle(code: String, reason: String) {
        val item = store.find(code) ?: error("dispute threshold $code not found")
        if (item.isTerminal()) {
            note("$code already closed")
            return
        }
        item.moveTo(DisputeThresholdStatus.ARCHIVED)
        store.save(item)
        note("$code is archived: $reason")
    }

    fun score(code: String): Double {
        val item = store.find(code) ?: error("dispute threshold $code not found")
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

fun newDisputeThresholdWorkflow(store: DisputeThresholdStore, clock: Clock, flags: Registry): DisputeThresholdWorkflow {
    return DisputeThresholdWorkflow(
        store = store,
        validator = defaultDisputeThresholdValidator(),
        clock = clock,
        journal = mutableListOf<String>(),
        engine = defaultDisputeThresholdEngine(flags),
    )
}
