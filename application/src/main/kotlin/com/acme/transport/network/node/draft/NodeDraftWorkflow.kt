package com.acme.transport.network.node.draft

import com.acme.transport.network.clock.Clock

/** Moves node drafts through their life cycle and keeps a journal of what it did. */
data class NodeDraftManager(
    var store: NodeDraftStore,
    var validator: NodeDraftValidator,
    var clock: Clock,
    var journal: MutableList<String>,
) {
    private fun note(message: String) {
        journal.add("${clock.now()} $message")
    }

    fun start(code: String) {
        val item = store.find(code) ?: error("node draft $code not found")
        validator.requireValid(item)
        item.moveTo(NodeDraftStatus.GRANTED)
        store.save(item)
        note("$code is granted")
    }

    fun retire(code: String, reason: String) {
        val item = store.find(code) ?: error("node draft $code not found")
        if (item.isTerminal()) {
            note("$code already closed")
            return
        }
        item.moveTo(NodeDraftStatus.EXPIRED)
        store.save(item)
        note("$code is expired: $reason")
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

fun newNodeDraftManager(store: NodeDraftStore, clock: Clock): NodeDraftManager {
    return NodeDraftManager(
        store = store,
        validator = defaultNodeDraftValidator(),
        clock = clock,
        journal = mutableListOf<String>(),
    )
}
