package com.acme.transport.network.node.cycle

import com.acme.transport.network.clock.Clock

/** Moves node digests through their life cycle and keeps a journal of what it did. */
data class NodeDigestCoordinator(
    var store: NodeDigestStore,
    var validator: NodeDigestValidator,
    var clock: Clock,
    var journal: MutableList<String>,
    var engine: NodeDigestEngine,
) {
    private fun note(message: String) {
        journal.add("${clock.now()} $message")
    }

    fun start(code: String) {
        val item = store.find(code) ?: error("node digest $code not found")
        validator.requireValid(item)
        item.moveTo(NodeDigestStatus.QUEUED)
        store.save(item)
        note("$code is queued")
    }

    fun finish(code: String, reason: String) {
        val item = store.find(code) ?: error("node digest $code not found")
        if (item.isTerminal()) {
            note("$code already closed")
            return
        }
        item.moveTo(NodeDigestStatus.FAILED)
        store.save(item)
        note("$code is failed: $reason")
    }

    fun quote(code: String): Double {
        val item = store.find(code) ?: error("node digest $code not found")
        val result = engine.compute(item)
        note("$code evaluated at $result")
        return result
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

fun newNodeDigestCoordinator(store: NodeDigestStore, clock: Clock): NodeDigestCoordinator {
    return NodeDigestCoordinator(
        store = store,
        validator = defaultNodeDigestValidator(),
        clock = clock,
        journal = mutableListOf<String>(),
        engine = defaultNodeDigestEngine(),
    )
}
