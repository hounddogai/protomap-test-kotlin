package com.acme.transport.network.node.seasonalnodeagreement

import com.acme.transport.network.clock.Clock

/** Moves node estimates through their life cycle and keeps a journal of what it did. */
data class NodeEstimateDesk(
    var store: NodeEstimateStore,
    var validator: NodeEstimateValidator,
    var clock: Clock,
    var journal: MutableList<String>,
) {
    private fun note(message: String) {
        journal.add("${clock.now()} $message")
    }

    fun start(code: String) {
        val item = store.find(code) ?: error("node estimate $code not found")
        validator.requireValid(item)
        item.moveTo(NodeEstimateStatus.WARMING)
        store.save(item)
        note("$code is warming")
    }

    fun settle(code: String, reason: String) {
        val item = store.find(code) ?: error("node estimate $code not found")
        if (item.isTerminal()) {
            note("$code already closed")
            return
        }
        item.moveTo(NodeEstimateStatus.STOPPED)
        store.save(item)
        note("$code is stopped: $reason")
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

fun newNodeEstimateDesk(store: NodeEstimateStore, clock: Clock): NodeEstimateDesk {
    return NodeEstimateDesk(
        store = store,
        validator = defaultNodeEstimateValidator(),
        clock = clock,
        journal = mutableListOf<String>(),
    )
}
