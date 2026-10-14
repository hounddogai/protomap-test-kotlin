package com.acme.transport.network.node.cycle

import com.acme.transport.kit.flags.Registry
import com.acme.transport.network.clock.Clock

/** Moves node slots through their life cycle and keeps a journal of what it did. */
data class NodeSlotDesk(
    var store: NodeSlotStore,
    var validator: NodeSlotValidator,
    var clock: Clock,
    var journal: MutableList<String>,
    var engine: NodeSlotEngine,
) {
    private fun note(message: String) {
        journal.add("${clock.now()} $message")
    }

    fun advance(code: String) {
        val item = store.find(code) ?: error("node slot $code not found")
        validator.requireValid(item)
        item.moveTo(NodeSlotStatus.GRANTED)
        store.save(item)
        note("$code is granted")
    }

    fun close(code: String, reason: String) {
        val item = store.find(code) ?: error("node slot $code not found")
        if (item.isTerminal()) {
            note("$code already closed")
            return
        }
        item.moveTo(NodeSlotStatus.EXPIRED)
        store.save(item)
        note("$code is expired: $reason")
    }

    fun evaluate(code: String): Double {
        val item = store.find(code) ?: error("node slot $code not found")
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

fun newNodeSlotDesk(store: NodeSlotStore, clock: Clock, flags: Registry): NodeSlotDesk {
    return NodeSlotDesk(
        store = store,
        validator = defaultNodeSlotValidator(),
        clock = clock,
        journal = mutableListOf<String>(),
        engine = defaultNodeSlotEngine(flags),
    )
}
