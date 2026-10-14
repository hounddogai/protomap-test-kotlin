package com.acme.transport.network.node.policy

import com.acme.transport.network.clock.Clock

/** Moves node policies through their life cycle and keeps a journal of what it did. */
data class NodePolicyManager(
    var store: NodePolicyStore,
    var validator: NodePolicyValidator,
    var clock: Clock,
    var journal: MutableList<String>,
    var engine: NodePolicyEngine,
) {
    private fun note(message: String) {
        journal.add("${clock.now()} $message")
    }

    fun advance(code: String) {
        val item = store.find(code) ?: error("node policy $code not found")
        validator.requireValid(item)
        item.moveTo(NodePolicyStatus.QUEUED)
        store.save(item)
        note("$code is queued")
    }

    fun complete(code: String, reason: String) {
        val item = store.find(code) ?: error("node policy $code not found")
        if (item.isTerminal()) {
            note("$code already closed")
            return
        }
        item.moveTo(NodePolicyStatus.FAILED)
        store.save(item)
        note("$code is failed: $reason")
    }

    fun estimate(code: String): Double {
        val item = store.find(code) ?: error("node policy $code not found")
        val result = engine.run(item)
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

fun newNodePolicyManager(store: NodePolicyStore, clock: Clock): NodePolicyManager {
    return NodePolicyManager(
        store = store,
        validator = defaultNodePolicyValidator(),
        clock = clock,
        journal = mutableListOf<String>(),
        engine = defaultNodePolicyEngine(),
    )
}
