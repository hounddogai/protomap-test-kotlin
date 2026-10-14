package com.acme.transport.network.node.cycle

import com.acme.transport.network.clock.Clock

/** Moves node requests through their life cycle and keeps a journal of what it did. */
data class NodeRequestCoordinator(
    var store: NodeRequestStore,
    var validator: NodeRequestValidator,
    var clock: Clock,
    var journal: MutableList<String>,
    var engine: NodeRequestEngine,
) {
    private fun note(message: String) {
        journal.add("${clock.now()} $message")
    }

    fun submit(code: String) {
        val item = store.find(code) ?: error("node request $code not found")
        validator.requireValid(item)
        item.moveTo(NodeRequestStatus.PENDING)
        store.save(item)
        note("$code is pending")
    }

    fun complete(code: String, reason: String) {
        val item = store.find(code) ?: error("node request $code not found")
        if (item.isTerminal()) {
            note("$code already closed")
            return
        }
        item.moveTo(NodeRequestStatus.CLOSED)
        store.save(item)
        note("$code is closed: $reason")
    }

    fun estimate(code: String): Double {
        val item = store.find(code) ?: error("node request $code not found")
        val result = engine.evaluate(item)
        note("$code evaluated at $result")
        return result
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

fun newNodeRequestCoordinator(store: NodeRequestStore, clock: Clock): NodeRequestCoordinator {
    return NodeRequestCoordinator(
        store = store,
        validator = defaultNodeRequestValidator(),
        clock = clock,
        journal = mutableListOf<String>(),
        engine = defaultNodeRequestEngine(),
    )
}
