package com.acme.transport.network.node.plan

import com.acme.transport.network.clock.Clock

/** Moves node plans through their life cycle and keeps a journal of what it did. */
data class NodePlanCoordinator(
    var store: NodePlanStore,
    var validator: NodePlanValidator,
    var clock: Clock,
    var journal: MutableList<String>,
    var engine: NodePlanEngine,
) {
    private fun note(message: String) {
        journal.add("${clock.now()} $message")
    }

    fun open(code: String) {
        val item = store.find(code) ?: error("node plan $code not found")
        validator.requireValid(item)
        item.moveTo(NodePlanStatus.SCHEDULED)
        store.save(item)
        note("$code is scheduled")
    }

    fun close(code: String, reason: String) {
        val item = store.find(code) ?: error("node plan $code not found")
        if (item.isTerminal()) {
            note("$code already closed")
            return
        }
        item.moveTo(NodePlanStatus.CANCELLED)
        store.save(item)
        note("$code is cancelled: $reason")
    }

    fun estimate(code: String): Double {
        val item = store.find(code) ?: error("node plan $code not found")
        val result = engine.compute(item)
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

fun newNodePlanCoordinator(store: NodePlanStore, clock: Clock): NodePlanCoordinator {
    return NodePlanCoordinator(
        store = store,
        validator = defaultNodePlanValidator(),
        clock = clock,
        journal = mutableListOf<String>(),
        engine = defaultNodePlanEngine(),
    )
}
