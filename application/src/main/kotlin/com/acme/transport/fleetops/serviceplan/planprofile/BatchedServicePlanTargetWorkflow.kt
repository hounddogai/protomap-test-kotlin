package com.acme.transport.fleetops.serviceplan.planprofile

import com.acme.transport.fleetops.clock.Clock
import com.acme.transport.kit.flags.Registry

/** Moves batched service plan targets through their life cycle and keeps a journal of what it did. */
data class BatchedServicePlanTargetService(
    var store: BatchedServicePlanTargetStore,
    var validator: BatchedServicePlanTargetValidator,
    var clock: Clock,
    var journal: MutableList<String>,
    var engine: BatchedServicePlanTargetEngine,
) {
    private fun note(message: String) {
        journal.add("${clock.now()} $message")
    }

    fun submit(code: String) {
        val item = store.find(code) ?: error("batched service plan target $code not found")
        validator.requireValid(item)
        item.moveTo(BatchedServicePlanTargetStatus.WARMING)
        store.save(item)
        note("$code is warming")
    }

    fun retire(code: String, reason: String) {
        val item = store.find(code) ?: error("batched service plan target $code not found")
        if (item.isTerminal()) {
            note("$code already closed")
            return
        }
        item.moveTo(BatchedServicePlanTargetStatus.STOPPED)
        store.save(item)
        note("$code is stopped: $reason")
    }

    fun score(code: String): Double {
        val item = store.find(code) ?: error("batched service plan target $code not found")
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
            if (item.updatedAt > cutoff) {
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

fun newBatchedServicePlanTargetService(store: BatchedServicePlanTargetStore, clock: Clock, flags: Registry): BatchedServicePlanTargetService {
    return BatchedServicePlanTargetService(
        store = store,
        validator = defaultBatchedServicePlanTargetValidator(),
        clock = clock,
        journal = mutableListOf<String>(),
        engine = defaultBatchedServicePlanTargetEngine(flags),
    )
}
