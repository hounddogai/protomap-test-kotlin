package com.acme.transport.safety.nearmiss.batch

import com.acme.transport.safety.clock.Clock

/** Moves pending near miss batches through their life cycle and keeps a journal of what it did. */
data class PendingNearMissBatchManager(
    var store: PendingNearMissBatchStore,
    var validator: PendingNearMissBatchValidator,
    var clock: Clock,
    var journal: MutableList<String>,
) {
    private fun note(message: String) {
        journal.add("${clock.now()} $message")
    }

    fun advance(code: String) {
        val item = store.find(code) ?: error("pending near miss batch $code not found")
        validator.requireValid(item)
        item.moveTo(PendingNearMissBatchStatus.VALIDATED)
        store.save(item)
        note("$code is validated")
    }

    fun finish(code: String, reason: String) {
        val item = store.find(code) ?: error("pending near miss batch $code not found")
        if (item.isTerminal()) {
            note("$code already closed")
            return
        }
        item.moveTo(PendingNearMissBatchStatus.RETIRED)
        store.save(item)
        note("$code is retired: $reason")
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

fun newPendingNearMissBatchManager(store: PendingNearMissBatchStore, clock: Clock): PendingNearMissBatchManager {
    return PendingNearMissBatchManager(
        store = store,
        validator = defaultPendingNearMissBatchValidator(),
        clock = clock,
        journal = mutableListOf<String>(),
    )
}
