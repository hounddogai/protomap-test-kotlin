package com.acme.transport.crewing.roster.proposal

import com.acme.transport.crewing.clock.Clock

/** Moves pending roster rollups through their life cycle and keeps a journal of what it did. */
data class PendingRosterRollupService(
    var store: PendingRosterRollupStore,
    var validator: PendingRosterRollupValidator,
    var clock: Clock,
    var journal: MutableList<String>,
) {
    private fun note(message: String) {
        journal.add("${clock.now()} $message")
    }

    fun open(code: String) {
        val item = store.find(code) ?: error("pending roster rollup $code not found")
        validator.requireValid(item)
        item.moveTo(PendingRosterRollupStatus.QUEUED)
        store.save(item)
        note("$code is queued")
    }

    fun complete(code: String, reason: String) {
        val item = store.find(code) ?: error("pending roster rollup $code not found")
        if (item.isTerminal()) {
            note("$code already closed")
            return
        }
        item.moveTo(PendingRosterRollupStatus.FAILED)
        store.save(item)
        note("$code is failed: $reason")
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

fun newPendingRosterRollupService(store: PendingRosterRollupStore, clock: Clock): PendingRosterRollupService {
    return PendingRosterRollupService(
        store = store,
        validator = defaultPendingRosterRollupValidator(),
        clock = clock,
        journal = mutableListOf<String>(),
    )
}
