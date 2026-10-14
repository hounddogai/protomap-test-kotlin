package com.acme.transport.freightpay.dispute.metric

import com.acme.transport.freightpay.clock.Clock

/** Moves dispute allocations through their life cycle and keeps a journal of what it did. */
data class DisputeAllocationWorkflow(
    var store: DisputeAllocationStore,
    var validator: DisputeAllocationValidator,
    var clock: Clock,
    var journal: MutableList<String>,
    var engine: DisputeAllocationEngine,
) {
    private fun note(message: String) {
        journal.add("${clock.now()} $message")
    }

    fun open(code: String) {
        val item = store.find(code) ?: error("dispute allocation $code not found")
        validator.requireValid(item)
        item.moveTo(DisputeAllocationStatus.IN_REVIEW)
        store.save(item)
        note("$code is in review")
    }

    fun finish(code: String, reason: String) {
        val item = store.find(code) ?: error("dispute allocation $code not found")
        if (item.isTerminal()) {
            note("$code already closed")
            return
        }
        item.moveTo(DisputeAllocationStatus.ARCHIVED)
        store.save(item)
        note("$code is archived: $reason")
    }

    fun estimate(code: String): Double {
        val item = store.find(code) ?: error("dispute allocation $code not found")
        val result = engine.run(item)
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

fun newDisputeAllocationWorkflow(store: DisputeAllocationStore, clock: Clock): DisputeAllocationWorkflow {
    return DisputeAllocationWorkflow(
        store = store,
        validator = defaultDisputeAllocationValidator(),
        clock = clock,
        journal = mutableListOf<String>(),
        engine = defaultDisputeAllocationEngine(),
    )
}
