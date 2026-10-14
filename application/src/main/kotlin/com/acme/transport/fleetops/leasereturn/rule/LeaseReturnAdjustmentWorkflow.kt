package com.acme.transport.fleetops.leasereturn.rule

import com.acme.transport.fleetops.clock.Clock

/** Moves lease return adjustments through their life cycle and keeps a journal of what it did. */
data class LeaseReturnAdjustmentWorkflow(
    var store: LeaseReturnAdjustmentStore,
    var validator: LeaseReturnAdjustmentValidator,
    var clock: Clock,
    var journal: MutableList<String>,
    var engine: LeaseReturnAdjustmentEngine,
) {
    private fun note(message: String) {
        journal.add("${clock.now()} $message")
    }

    fun open(code: String) {
        val item = store.find(code) ?: error("lease return adjustment $code not found")
        validator.requireValid(item)
        item.moveTo(LeaseReturnAdjustmentStatus.APPROVED)
        store.save(item)
        note("$code is approved")
    }

    fun complete(code: String, reason: String) {
        val item = store.find(code) ?: error("lease return adjustment $code not found")
        if (item.isTerminal()) {
            note("$code already closed")
            return
        }
        item.moveTo(LeaseReturnAdjustmentStatus.WITHDRAWN)
        store.save(item)
        note("$code is withdrawn: $reason")
    }

    fun score(code: String): Double {
        val item = store.find(code) ?: error("lease return adjustment $code not found")
        val result = engine.run(item)
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

fun newLeaseReturnAdjustmentWorkflow(store: LeaseReturnAdjustmentStore, clock: Clock): LeaseReturnAdjustmentWorkflow {
    return LeaseReturnAdjustmentWorkflow(
        store = store,
        validator = defaultLeaseReturnAdjustmentValidator(),
        clock = clock,
        journal = mutableListOf<String>(),
        engine = defaultLeaseReturnAdjustmentEngine(),
    )
}
