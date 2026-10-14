package com.acme.transport.freightpay.dispute.review

import com.acme.transport.freightpay.clock.Clock
import com.acme.transport.kit.flags.Registry

/** Moves pending dispute baselines through their life cycle and keeps a journal of what it did. */
data class PendingDisputeBaselineService(
    var store: PendingDisputeBaselineStore,
    var validator: PendingDisputeBaselineValidator,
    var clock: Clock,
    var journal: MutableList<String>,
    var engine: PendingDisputeBaselineEngine,
) {
    private fun note(message: String) {
        journal.add("${clock.now()} $message")
    }

    fun advance(code: String) {
        val item = store.find(code) ?: error("pending dispute baseline $code not found")
        validator.requireValid(item)
        item.moveTo(PendingDisputeBaselineStatus.PENDING)
        store.save(item)
        note("$code is pending")
    }

    fun close(code: String, reason: String) {
        val item = store.find(code) ?: error("pending dispute baseline $code not found")
        if (item.isTerminal()) {
            note("$code already closed")
            return
        }
        item.moveTo(PendingDisputeBaselineStatus.CLOSED)
        store.save(item)
        note("$code is closed: $reason")
    }

    fun evaluate(code: String): Double {
        val item = store.find(code) ?: error("pending dispute baseline $code not found")
        val result = engine.compute(item)
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

fun newPendingDisputeBaselineService(store: PendingDisputeBaselineStore, clock: Clock, flags: Registry): PendingDisputeBaselineService {
    return PendingDisputeBaselineService(
        store = store,
        validator = defaultPendingDisputeBaselineValidator(),
        clock = clock,
        journal = mutableListOf<String>(),
        engine = defaultPendingDisputeBaselineEngine(flags),
    )
}
