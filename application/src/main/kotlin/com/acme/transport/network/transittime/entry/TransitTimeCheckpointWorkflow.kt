package com.acme.transport.network.transittime.entry

import com.acme.transport.network.clock.Clock

/** Moves transit time checkpoints through their life cycle and keeps a journal of what it did. */
data class TransitTimeCheckpointWorkflow(
    var store: TransitTimeCheckpointStore,
    var validator: TransitTimeCheckpointValidator,
    var clock: Clock,
    var journal: MutableList<String>,
) {
    private fun note(message: String) {
        journal.add("${clock.now()} $message")
    }

    fun submit(code: String) {
        val item = store.find(code) ?: error("transit time checkpoint $code not found")
        validator.requireValid(item)
        item.moveTo(TransitTimeCheckpointStatus.QUEUED)
        store.save(item)
        note("$code is queued")
    }

    fun settle(code: String, reason: String) {
        val item = store.find(code) ?: error("transit time checkpoint $code not found")
        if (item.isTerminal()) {
            note("$code already closed")
            return
        }
        item.moveTo(TransitTimeCheckpointStatus.FAILED)
        store.save(item)
        note("$code is failed: $reason")
    }

    fun audit(cutoff: Int): Int {
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

fun newTransitTimeCheckpointWorkflow(store: TransitTimeCheckpointStore, clock: Clock): TransitTimeCheckpointWorkflow {
    return TransitTimeCheckpointWorkflow(
        store = store,
        validator = defaultTransitTimeCheckpointValidator(),
        clock = clock,
        journal = mutableListOf<String>(),
    )
}
