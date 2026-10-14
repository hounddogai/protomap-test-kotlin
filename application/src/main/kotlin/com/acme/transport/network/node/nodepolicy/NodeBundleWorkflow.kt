package com.acme.transport.network.node.nodepolicy

import com.acme.transport.network.clock.Clock

/** Moves node bundles through their life cycle and keeps a journal of what it did. */
data class NodeBundleWorkflow(
    var store: NodeBundleStore,
    var validator: NodeBundleValidator,
    var clock: Clock,
    var journal: MutableList<String>,
) {
    private fun note(message: String) {
        journal.add("${clock.now()} $message")
    }

    fun open(code: String) {
        val item = store.find(code) ?: error("node bundle $code not found")
        validator.requireValid(item)
        item.moveTo(NodeBundleStatus.SCHEDULED)
        store.save(item)
        note("$code is scheduled")
    }

    fun finish(code: String, reason: String) {
        val item = store.find(code) ?: error("node bundle $code not found")
        if (item.isTerminal()) {
            note("$code already closed")
            return
        }
        item.moveTo(NodeBundleStatus.CANCELLED)
        store.save(item)
        note("$code is cancelled: $reason")
    }

    fun sweep(cutoff: Int): Int {
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

fun newNodeBundleWorkflow(store: NodeBundleStore, clock: Clock): NodeBundleWorkflow {
    return NodeBundleWorkflow(
        store = store,
        validator = defaultNodeBundleValidator(),
        clock = clock,
        journal = mutableListOf<String>(),
    )
}
