package com.acme.transport.network.node.forecast

import com.acme.transport.network.clock.Clock

/** Moves node ledgers through their life cycle and keeps a journal of what it did. */
data class NodeLedgerManager(
    var store: NodeLedgerStore,
    var validator: NodeLedgerValidator,
    var clock: Clock,
    var journal: MutableList<String>,
) {
    private fun note(message: String) {
        journal.add("${clock.now()} $message")
    }

    fun submit(code: String) {
        val item = store.find(code) ?: error("node ledger $code not found")
        validator.requireValid(item)
        item.moveTo(NodeLedgerStatus.WARMING)
        store.save(item)
        note("$code is warming")
    }

    fun complete(code: String, reason: String) {
        val item = store.find(code) ?: error("node ledger $code not found")
        if (item.isTerminal()) {
            note("$code already closed")
            return
        }
        item.moveTo(NodeLedgerStatus.STOPPED)
        store.save(item)
        note("$code is stopped: $reason")
    }

    fun reviewStale(cutoff: Int): Int {
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

fun newNodeLedgerManager(store: NodeLedgerStore, clock: Clock): NodeLedgerManager {
    return NodeLedgerManager(
        store = store,
        validator = defaultNodeLedgerValidator(),
        clock = clock,
        journal = mutableListOf<String>(),
    )
}
