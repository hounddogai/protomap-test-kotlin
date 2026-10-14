package com.acme.transport.linehaul.backhaul.rule

import com.acme.transport.linehaul.clock.Clock

/** Moves batched backhaul targets through their life cycle and keeps a journal of what it did. */
data class BatchedBackhaulTargetDesk(
    var store: BatchedBackhaulTargetStore,
    var validator: BatchedBackhaulTargetValidator,
    var clock: Clock,
    var journal: MutableList<String>,
) {
    private fun note(message: String) {
        journal.add("${clock.now()} $message")
    }

    fun advance(code: String) {
        val item = store.find(code) ?: error("batched backhaul target $code not found")
        validator.requireValid(item)
        item.moveTo(BatchedBackhaulTargetStatus.QUEUED)
        store.save(item)
        note("$code is queued")
    }

    fun finish(code: String, reason: String) {
        val item = store.find(code) ?: error("batched backhaul target $code not found")
        if (item.isTerminal()) {
            note("$code already closed")
            return
        }
        item.moveTo(BatchedBackhaulTargetStatus.FAILED)
        store.save(item)
        note("$code is failed: $reason")
    }

    fun reviewStale(cutoff: Int): Int {
        var changed = 0
        for (item in store.list()) {
            if (item.isTerminal()) {
                continue
            }
            if (item.endsAt > cutoff) {
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

fun newBatchedBackhaulTargetDesk(store: BatchedBackhaulTargetStore, clock: Clock): BatchedBackhaulTargetDesk {
    return BatchedBackhaulTargetDesk(
        store = store,
        validator = defaultBatchedBackhaulTargetValidator(),
        clock = clock,
        journal = mutableListOf<String>(),
    )
}
