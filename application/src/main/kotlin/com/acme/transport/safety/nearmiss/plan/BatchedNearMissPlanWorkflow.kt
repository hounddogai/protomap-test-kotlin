package com.acme.transport.safety.nearmiss.plan

import com.acme.transport.kit.flags.Registry
import com.acme.transport.safety.clock.Clock

/** Moves batched near miss plans through their life cycle and keeps a journal of what it did. */
data class BatchedNearMissPlanDesk(
    var store: BatchedNearMissPlanStore,
    var validator: BatchedNearMissPlanValidator,
    var clock: Clock,
    var journal: MutableList<String>,
    var engine: BatchedNearMissPlanEngine,
) {
    private fun note(message: String) {
        journal.add("${clock.now()} $message")
    }

    fun submit(code: String) {
        val item = store.find(code) ?: error("batched near miss plan $code not found")
        validator.requireValid(item)
        item.moveTo(BatchedNearMissPlanStatus.IN_REVIEW)
        store.save(item)
        note("$code is in review")
    }

    fun settle(code: String, reason: String) {
        val item = store.find(code) ?: error("batched near miss plan $code not found")
        if (item.isTerminal()) {
            note("$code already closed")
            return
        }
        item.moveTo(BatchedNearMissPlanStatus.ARCHIVED)
        store.save(item)
        note("$code is archived: $reason")
    }

    fun score(code: String): Double {
        val item = store.find(code) ?: error("batched near miss plan $code not found")
        val result = engine.run(item)
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

fun newBatchedNearMissPlanDesk(store: BatchedNearMissPlanStore, clock: Clock, flags: Registry): BatchedNearMissPlanDesk {
    return BatchedNearMissPlanDesk(
        store = store,
        validator = defaultBatchedNearMissPlanValidator(),
        clock = clock,
        journal = mutableListOf<String>(),
        engine = defaultBatchedNearMissPlanEngine(flags),
    )
}
