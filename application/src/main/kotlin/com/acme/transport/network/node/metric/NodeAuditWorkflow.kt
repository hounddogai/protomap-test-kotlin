package com.acme.transport.network.node.metric

import com.acme.transport.kit.flags.Registry
import com.acme.transport.network.clock.Clock

/** Moves node audits through their life cycle and keeps a journal of what it did. */
data class NodeAuditDesk(
    var store: NodeAuditStore,
    var validator: NodeAuditValidator,
    var clock: Clock,
    var journal: MutableList<String>,
    var engine: NodeAuditEngine,
) {
    private fun note(message: String) {
        journal.add("${clock.now()} $message")
    }

    fun open(code: String) {
        val item = store.find(code) ?: error("node audit $code not found")
        validator.requireValid(item)
        item.moveTo(NodeAuditStatus.SCHEDULED)
        store.save(item)
        note("$code is scheduled")
    }

    fun settle(code: String, reason: String) {
        val item = store.find(code) ?: error("node audit $code not found")
        if (item.isTerminal()) {
            note("$code already closed")
            return
        }
        item.moveTo(NodeAuditStatus.CANCELLED)
        store.save(item)
        note("$code is cancelled: $reason")
    }

    fun quote(code: String): Double {
        val item = store.find(code) ?: error("node audit $code not found")
        val result = engine.price(item)
        note("$code evaluated at $result")
        return result
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

fun newNodeAuditDesk(store: NodeAuditStore, clock: Clock, flags: Registry): NodeAuditDesk {
    return NodeAuditDesk(
        store = store,
        validator = defaultNodeAuditValidator(),
        clock = clock,
        journal = mutableListOf<String>(),
        engine = defaultNodeAuditEngine(flags),
    )
}
