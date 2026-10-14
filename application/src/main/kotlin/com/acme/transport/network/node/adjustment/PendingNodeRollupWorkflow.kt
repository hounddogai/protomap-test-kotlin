package com.acme.transport.network.node.adjustment

import com.acme.transport.network.clock.Clock

/** Moves pending node rollups through their life cycle and keeps a journal of what it did. */
data class PendingNodeRollupCoordinator(
    var store: PendingNodeRollupStore,
    var validator: PendingNodeRollupValidator,
    var clock: Clock,
    var journal: MutableList<String>,
) {
    private fun note(message: String) {
        journal.add("${clock.now()} $message")
    }

    fun start(code: String) {
        val item = store.find(code) ?: error("pending node rollup $code not found")
        validator.requireValid(item)
        item.moveTo(PendingNodeRollupStatus.APPROVED)
        store.save(item)
        note("$code is approved")
    }

    fun close(code: String, reason: String) {
        val item = store.find(code) ?: error("pending node rollup $code not found")
        if (item.isTerminal()) {
            note("$code already closed")
            return
        }
        item.moveTo(PendingNodeRollupStatus.WITHDRAWN)
        store.save(item)
        note("$code is withdrawn: $reason")
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

fun newPendingNodeRollupCoordinator(store: PendingNodeRollupStore, clock: Clock): PendingNodeRollupCoordinator {
    return PendingNodeRollupCoordinator(
        store = store,
        validator = defaultPendingNodeRollupValidator(),
        clock = clock,
        journal = mutableListOf<String>(),
    )
}
