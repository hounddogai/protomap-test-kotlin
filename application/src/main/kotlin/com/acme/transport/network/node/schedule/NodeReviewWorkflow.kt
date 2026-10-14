package com.acme.transport.network.node.schedule

import com.acme.transport.kit.flags.Registry
import com.acme.transport.network.clock.Clock

/** Moves node reviews through their life cycle and keeps a journal of what it did. */
data class NodeReviewWorkflow(
    var store: NodeReviewStore,
    var validator: NodeReviewValidator,
    var clock: Clock,
    var journal: MutableList<String>,
    var engine: NodeReviewEngine,
) {
    private fun note(message: String) {
        journal.add("${clock.now()} $message")
    }

    fun advance(code: String) {
        val item = store.find(code) ?: error("node review $code not found")
        validator.requireValid(item)
        item.moveTo(NodeReviewStatus.QUEUED)
        store.save(item)
        note("$code is queued")
    }

    fun finish(code: String, reason: String) {
        val item = store.find(code) ?: error("node review $code not found")
        if (item.isTerminal()) {
            note("$code already closed")
            return
        }
        item.moveTo(NodeReviewStatus.FAILED)
        store.save(item)
        note("$code is failed: $reason")
    }

    fun evaluate(code: String): Double {
        val item = store.find(code) ?: error("node review $code not found")
        val result = engine.run(item)
        note("$code evaluated at $result")
        return result
    }

    fun sweep(cutoff: Int): Int {
        var changed = 0
        for (item in store.list()) {
            if (item.isTerminal()) {
                continue
            }
            if (item.startsAt > cutoff) {
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

fun newNodeReviewWorkflow(store: NodeReviewStore, clock: Clock, flags: Registry): NodeReviewWorkflow {
    return NodeReviewWorkflow(
        store = store,
        validator = defaultNodeReviewValidator(),
        clock = clock,
        journal = mutableListOf<String>(),
        engine = defaultNodeReviewEngine(flags),
    )
}
