package com.acme.transport.network.node.schedule

import com.acme.transport.kit.flags.Registry
import com.acme.transport.network.clock.Clock

/** Moves node tiers through their life cycle and keeps a journal of what it did. */
data class NodeTierService(
    var store: NodeTierStore,
    var validator: NodeTierValidator,
    var clock: Clock,
    var journal: MutableList<String>,
    var engine: NodeTierEngine,
) {
    private fun note(message: String) {
        journal.add("${clock.now()} $message")
    }

    fun start(code: String) {
        val item = store.find(code) ?: error("node tier $code not found")
        validator.requireValid(item)
        item.moveTo(NodeTierStatus.GRANTED)
        store.save(item)
        note("$code is granted")
    }

    fun retire(code: String, reason: String) {
        val item = store.find(code) ?: error("node tier $code not found")
        if (item.isTerminal()) {
            note("$code already closed")
            return
        }
        item.moveTo(NodeTierStatus.EXPIRED)
        store.save(item)
        note("$code is expired: $reason")
    }

    fun score(code: String): Double {
        val item = store.find(code) ?: error("node tier $code not found")
        val result = engine.price(item)
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

fun newNodeTierService(store: NodeTierStore, clock: Clock, flags: Registry): NodeTierService {
    return NodeTierService(
        store = store,
        validator = defaultNodeTierValidator(),
        clock = clock,
        journal = mutableListOf<String>(),
        engine = defaultNodeTierEngine(flags),
    )
}
