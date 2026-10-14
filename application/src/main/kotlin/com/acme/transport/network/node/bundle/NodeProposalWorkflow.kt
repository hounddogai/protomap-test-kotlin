package com.acme.transport.network.node.bundle

import com.acme.transport.network.clock.Clock

/** Moves node proposals through their life cycle and keeps a journal of what it did. */
data class NodeProposalManager(
    var store: NodeProposalStore,
    var validator: NodeProposalValidator,
    var clock: Clock,
    var journal: MutableList<String>,
) {
    private fun note(message: String) {
        journal.add("${clock.now()} $message")
    }

    fun start(code: String) {
        val item = store.find(code) ?: error("node proposal $code not found")
        validator.requireValid(item)
        item.moveTo(NodeProposalStatus.VALIDATED)
        store.save(item)
        note("$code is validated")
    }

    fun close(code: String, reason: String) {
        val item = store.find(code) ?: error("node proposal $code not found")
        if (item.isTerminal()) {
            note("$code already closed")
            return
        }
        item.moveTo(NodeProposalStatus.RETIRED)
        store.save(item)
        note("$code is retired: $reason")
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

fun newNodeProposalManager(store: NodeProposalStore, clock: Clock): NodeProposalManager {
    return NodeProposalManager(
        store = store,
        validator = defaultNodeProposalValidator(),
        clock = clock,
        journal = mutableListOf<String>(),
    )
}
