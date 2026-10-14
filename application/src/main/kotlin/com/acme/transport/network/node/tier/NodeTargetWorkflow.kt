package com.acme.transport.network.node.tier

import com.acme.transport.network.clock.Clock

/** Moves node targets through their life cycle and keeps a journal of what it did. */
data class NodeTargetDesk(
    var store: NodeTargetStore,
    var validator: NodeTargetValidator,
    var clock: Clock,
    var journal: MutableList<String>,
    var engine: NodeTargetEngine,
) {
    private fun note(message: String) {
        journal.add("${clock.now()} $message")
    }

    fun open(code: String) {
        val item = store.find(code) ?: error("node target $code not found")
        validator.requireValid(item)
        item.moveTo(NodeTargetStatus.IN_REVIEW)
        store.save(item)
        note("$code is in review")
    }

    fun complete(code: String, reason: String) {
        val item = store.find(code) ?: error("node target $code not found")
        if (item.isTerminal()) {
            note("$code already closed")
            return
        }
        item.moveTo(NodeTargetStatus.ARCHIVED)
        store.save(item)
        note("$code is archived: $reason")
    }

    fun estimate(code: String): Double {
        val item = store.find(code) ?: error("node target $code not found")
        val result = engine.evaluate(item)
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

fun newNodeTargetDesk(store: NodeTargetStore, clock: Clock): NodeTargetDesk {
    return NodeTargetDesk(
        store = store,
        validator = defaultNodeTargetValidator(),
        clock = clock,
        journal = mutableListOf<String>(),
        engine = defaultNodeTargetEngine(),
    )
}
