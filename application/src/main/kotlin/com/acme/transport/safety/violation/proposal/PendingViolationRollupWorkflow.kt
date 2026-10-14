package com.acme.transport.safety.violation.proposal

import com.acme.transport.safety.clock.Clock

/** Moves pending violation rollups through their life cycle and keeps a journal of what it did. */
data class PendingViolationRollupWorkflow(
    var store: PendingViolationRollupStore,
    var validator: PendingViolationRollupValidator,
    var clock: Clock,
    var journal: MutableList<String>,
) {
    private fun note(message: String) {
        journal.add("${clock.now()} $message")
    }

    fun start(code: String) {
        val item = store.find(code) ?: error("pending violation rollup $code not found")
        validator.requireValid(item)
        item.moveTo(PendingViolationRollupStatus.WARMING)
        store.save(item)
        note("$code is warming")
    }

    fun retire(code: String, reason: String) {
        val item = store.find(code) ?: error("pending violation rollup $code not found")
        if (item.isTerminal()) {
            note("$code already closed")
            return
        }
        item.moveTo(PendingViolationRollupStatus.STOPPED)
        store.save(item)
        note("$code is stopped: $reason")
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

fun newPendingViolationRollupWorkflow(store: PendingViolationRollupStore, clock: Clock): PendingViolationRollupWorkflow {
    return PendingViolationRollupWorkflow(
        store = store,
        validator = defaultPendingViolationRollupValidator(),
        clock = clock,
        journal = mutableListOf<String>(),
    )
}
