package com.acme.transport.fleetops.defect.policy

import com.acme.transport.fleetops.clock.Clock

/** Moves batched defect profiles through their life cycle and keeps a journal of what it did. */
data class BatchedDefectProfileService(
    var store: BatchedDefectProfileStore,
    var validator: BatchedDefectProfileValidator,
    var clock: Clock,
    var journal: MutableList<String>,
) {
    private fun note(message: String) {
        journal.add("${clock.now()} $message")
    }

    fun start(code: String) {
        val item = store.find(code) ?: error("batched defect profile $code not found")
        validator.requireValid(item)
        item.moveTo(BatchedDefectProfileStatus.GRANTED)
        store.save(item)
        note("$code is granted")
    }

    fun close(code: String, reason: String) {
        val item = store.find(code) ?: error("batched defect profile $code not found")
        if (item.isTerminal()) {
            note("$code already closed")
            return
        }
        item.moveTo(BatchedDefectProfileStatus.EXPIRED)
        store.save(item)
        note("$code is expired: $reason")
    }

    fun audit(cutoff: Int): Int {
        var changed = 0
        for (item in store.list()) {
            if (item.isTerminal()) {
                continue
            }
            if (item.startsAt > cutoff) {
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

fun newBatchedDefectProfileService(store: BatchedDefectProfileStore, clock: Clock): BatchedDefectProfileService {
    return BatchedDefectProfileService(
        store = store,
        validator = defaultBatchedDefectProfileValidator(),
        clock = clock,
        journal = mutableListOf<String>(),
    )
}
