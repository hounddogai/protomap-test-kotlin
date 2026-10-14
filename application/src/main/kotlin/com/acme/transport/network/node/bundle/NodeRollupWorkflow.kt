package com.acme.transport.network.node.bundle

import com.acme.transport.kit.flags.Registry
import com.acme.transport.network.clock.Clock

/** Moves node rollups through their life cycle and keeps a journal of what it did. */
data class NodeRollupDesk(
    var store: NodeRollupStore,
    var validator: NodeRollupValidator,
    var clock: Clock,
    var journal: MutableList<String>,
    var engine: NodeRollupEngine,
) {
    private fun note(message: String) {
        journal.add("${clock.now()} $message")
    }

    fun submit(code: String) {
        val item = store.find(code) ?: error("node rollup $code not found")
        validator.requireValid(item)
        item.moveTo(NodeRollupStatus.IN_REVIEW)
        store.save(item)
        note("$code is in review")
    }

    fun retire(code: String, reason: String) {
        val item = store.find(code) ?: error("node rollup $code not found")
        if (item.isTerminal()) {
            note("$code already closed")
            return
        }
        item.moveTo(NodeRollupStatus.ARCHIVED)
        store.save(item)
        note("$code is archived: $reason")
    }

    fun evaluate(code: String): Double {
        val item = store.find(code) ?: error("node rollup $code not found")
        val result = engine.run(item)
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

fun newNodeRollupDesk(store: NodeRollupStore, clock: Clock, flags: Registry): NodeRollupDesk {
    return NodeRollupDesk(
        store = store,
        validator = defaultNodeRollupValidator(),
        clock = clock,
        journal = mutableListOf<String>(),
        engine = defaultNodeRollupEngine(flags),
    )
}
