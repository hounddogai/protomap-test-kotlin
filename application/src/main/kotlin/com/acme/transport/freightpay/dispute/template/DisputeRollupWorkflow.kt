package com.acme.transport.freightpay.dispute.template

import com.acme.transport.freightpay.clock.Clock
import com.acme.transport.kit.flags.Registry

/** Moves dispute rollups through their life cycle and keeps a journal of what it did. */
data class DisputeRollupWorkflow(
    var store: DisputeRollupStore,
    var validator: DisputeRollupValidator,
    var clock: Clock,
    var journal: MutableList<String>,
    var engine: DisputeRollupEngine,
) {
    private fun note(message: String) {
        journal.add("${clock.now()} $message")
    }

    fun open(code: String) {
        val item = store.find(code) ?: error("dispute rollup $code not found")
        validator.requireValid(item)
        item.moveTo(DisputeRollupStatus.PENDING)
        store.save(item)
        note("$code is pending")
    }

    fun retire(code: String, reason: String) {
        val item = store.find(code) ?: error("dispute rollup $code not found")
        if (item.isTerminal()) {
            note("$code already closed")
            return
        }
        item.moveTo(DisputeRollupStatus.CLOSED)
        store.save(item)
        note("$code is closed: $reason")
    }

    fun quote(code: String): Double {
        val item = store.find(code) ?: error("dispute rollup $code not found")
        val result = engine.compute(item)
        note("$code evaluated at $result")
        return result
    }

    fun audit(cutoff: Int): Int {
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

fun newDisputeRollupWorkflow(store: DisputeRollupStore, clock: Clock, flags: Registry): DisputeRollupWorkflow {
    return DisputeRollupWorkflow(
        store = store,
        validator = defaultDisputeRollupValidator(),
        clock = clock,
        journal = mutableListOf<String>(),
        engine = defaultDisputeRollupEngine(flags),
    )
}
