package com.acme.transport.network.node.schedule

import com.acme.transport.kit.flags.Registry
import com.acme.transport.network.clock.Clock

/** Moves node schedules through their life cycle and keeps a journal of what it did. */
data class NodeScheduleWorkflow(
    var store: NodeScheduleStore,
    var validator: NodeScheduleValidator,
    var clock: Clock,
    var journal: MutableList<String>,
    var engine: NodeScheduleEngine,
) {
    private fun note(message: String) {
        journal.add("${clock.now()} $message")
    }

    fun open(code: String) {
        val item = store.find(code) ?: error("node schedule $code not found")
        validator.requireValid(item)
        item.moveTo(NodeScheduleStatus.SCHEDULED)
        store.save(item)
        note("$code is scheduled")
    }

    fun settle(code: String, reason: String) {
        val item = store.find(code) ?: error("node schedule $code not found")
        if (item.isTerminal()) {
            note("$code already closed")
            return
        }
        item.moveTo(NodeScheduleStatus.CANCELLED)
        store.save(item)
        note("$code is cancelled: $reason")
    }

    fun estimate(code: String): Double {
        val item = store.find(code) ?: error("node schedule $code not found")
        val result = engine.evaluate(item)
        note("$code evaluated at $result")
        return result
    }

    fun reviewStale(cutoff: Int): Int {
        var changed = 0
        for (item in store.list()) {
            if (item.isTerminal()) {
                continue
            }
            if (item.expiresAt > cutoff) {
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

fun newNodeScheduleWorkflow(store: NodeScheduleStore, clock: Clock, flags: Registry): NodeScheduleWorkflow {
    return NodeScheduleWorkflow(
        store = store,
        validator = defaultNodeScheduleValidator(),
        clock = clock,
        journal = mutableListOf<String>(),
        engine = defaultNodeScheduleEngine(flags),
    )
}
