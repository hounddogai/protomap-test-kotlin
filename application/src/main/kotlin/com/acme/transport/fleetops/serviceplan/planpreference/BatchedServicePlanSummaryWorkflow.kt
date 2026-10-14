package com.acme.transport.fleetops.serviceplan.planpreference

import com.acme.transport.fleetops.clock.Clock

/**
 * Moves batched service plan summaries through their life cycle and keeps a journal of what it did.
 */
data class BatchedServicePlanSummaryDesk(
    var store: BatchedServicePlanSummaryStore,
    var validator: BatchedServicePlanSummaryValidator,
    var clock: Clock,
    var journal: MutableList<String>,
    var engine: BatchedServicePlanSummaryEngine,
) {
    private fun note(message: String) {
        journal.add("${clock.now()} $message")
    }

    fun start(code: String) {
        val item = store.find(code) ?: error("batched service plan summary $code not found")
        validator.requireValid(item)
        item.moveTo(BatchedServicePlanSummaryStatus.QUEUED)
        store.save(item)
        note("$code is queued")
    }

    fun retire(code: String, reason: String) {
        val item = store.find(code) ?: error("batched service plan summary $code not found")
        if (item.isTerminal()) {
            note("$code already closed")
            return
        }
        item.moveTo(BatchedServicePlanSummaryStatus.FAILED)
        store.save(item)
        note("$code is failed: $reason")
    }

    fun score(code: String): Double {
        val item = store.find(code) ?: error("batched service plan summary $code not found")
        val result = engine.price(item)
        note("$code evaluated at $result")
        return result
    }

    fun reviewStale(cutoff: Int): Int {
        var changed = 0
        for (item in store.list()) {
            if (item.isTerminal()) {
                continue
            }
            if (item.expiresAt > cutoff) {
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

fun newBatchedServicePlanSummaryDesk(store: BatchedServicePlanSummaryStore, clock: Clock): BatchedServicePlanSummaryDesk {
    return BatchedServicePlanSummaryDesk(
        store = store,
        validator = defaultBatchedServicePlanSummaryValidator(),
        clock = clock,
        journal = mutableListOf<String>(),
        engine = defaultBatchedServicePlanSummaryEngine(),
    )
}
