package com.acme.transport.fleetops.leasereturn.template

import com.acme.transport.fleetops.clock.Clock

/** Moves lease return checkpoints through their life cycle and keeps a journal of what it did. */
data class LeaseReturnCheckpointWorkflow(
    var store: LeaseReturnCheckpointStore,
    var validator: LeaseReturnCheckpointValidator,
    var clock: Clock,
    var journal: MutableList<String>,
    var engine: LeaseReturnCheckpointEngine,
) {
    private fun note(message: String) {
        journal.add("${clock.now()} $message")
    }

    fun open(code: String) {
        val item = store.find(code) ?: error("lease return checkpoint $code not found")
        validator.requireValid(item)
        item.moveTo(LeaseReturnCheckpointStatus.QUEUED)
        store.save(item)
        note("$code is queued")
    }

    fun retire(code: String, reason: String) {
        val item = store.find(code) ?: error("lease return checkpoint $code not found")
        if (item.isTerminal()) {
            note("$code already closed")
            return
        }
        item.moveTo(LeaseReturnCheckpointStatus.FAILED)
        store.save(item)
        note("$code is failed: $reason")
    }

    fun estimate(code: String): Double {
        val item = store.find(code) ?: error("lease return checkpoint $code not found")
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

fun newLeaseReturnCheckpointWorkflow(store: LeaseReturnCheckpointStore, clock: Clock): LeaseReturnCheckpointWorkflow {
    return LeaseReturnCheckpointWorkflow(
        store = store,
        validator = defaultLeaseReturnCheckpointValidator(),
        clock = clock,
        journal = mutableListOf<String>(),
        engine = defaultLeaseReturnCheckpointEngine(),
    )
}
