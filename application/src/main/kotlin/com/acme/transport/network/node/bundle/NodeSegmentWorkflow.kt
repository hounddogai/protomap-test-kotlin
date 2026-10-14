package com.acme.transport.network.node.bundle

import com.acme.transport.network.clock.Clock

/** Moves node segments through their life cycle and keeps a journal of what it did. */
data class NodeSegmentManager(
    var store: NodeSegmentStore,
    var validator: NodeSegmentValidator,
    var clock: Clock,
    var journal: MutableList<String>,
    var engine: NodeSegmentEngine,
) {
    private fun note(message: String) {
        journal.add("${clock.now()} $message")
    }

    fun open(code: String) {
        val item = store.find(code) ?: error("node segment $code not found")
        validator.requireValid(item)
        item.moveTo(NodeSegmentStatus.APPROVED)
        store.save(item)
        note("$code is approved")
    }

    fun complete(code: String, reason: String) {
        val item = store.find(code) ?: error("node segment $code not found")
        if (item.isTerminal()) {
            note("$code already closed")
            return
        }
        item.moveTo(NodeSegmentStatus.WITHDRAWN)
        store.save(item)
        note("$code is withdrawn: $reason")
    }

    fun estimate(code: String): Double {
        val item = store.find(code) ?: error("node segment $code not found")
        val result = engine.evaluate(item)
        note("$code evaluated at $result")
        return result
    }

    fun scan(cutoff: Int): Int {
        var changed = 0
        for (item in store.list()) {
            if (item.isTerminal()) {
                continue
            }
            if (item.createdAt > cutoff) {
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

fun newNodeSegmentManager(store: NodeSegmentStore, clock: Clock): NodeSegmentManager {
    return NodeSegmentManager(
        store = store,
        validator = defaultNodeSegmentValidator(),
        clock = clock,
        journal = mutableListOf<String>(),
        engine = defaultNodeSegmentEngine(),
    )
}
