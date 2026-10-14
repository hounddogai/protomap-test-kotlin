package com.acme.transport.network.node.milestone

import com.acme.transport.network.clock.Clock

/** Moves node thresholds through their life cycle and keeps a journal of what it did. */
data class NodeThresholdCoordinator(
    var store: NodeThresholdStore,
    var validator: NodeThresholdValidator,
    var clock: Clock,
    var journal: MutableList<String>,
) {
    private fun note(message: String) {
        journal.add("${clock.now()} $message")
    }

    fun open(code: String) {
        val item = store.find(code) ?: error("node threshold $code not found")
        validator.requireValid(item)
        item.moveTo(NodeThresholdStatus.IN_REVIEW)
        store.save(item)
        note("$code is in review")
    }

    fun complete(code: String, reason: String) {
        val item = store.find(code) ?: error("node threshold $code not found")
        if (item.isTerminal()) {
            note("$code already closed")
            return
        }
        item.moveTo(NodeThresholdStatus.ARCHIVED)
        store.save(item)
        note("$code is archived: $reason")
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

fun newNodeThresholdCoordinator(store: NodeThresholdStore, clock: Clock): NodeThresholdCoordinator {
    return NodeThresholdCoordinator(
        store = store,
        validator = defaultNodeThresholdValidator(),
        clock = clock,
        journal = mutableListOf<String>(),
    )
}
