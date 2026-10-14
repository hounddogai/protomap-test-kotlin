package com.acme.transport.network.node.plan

import com.acme.transport.network.clock.Clock

/** Moves node mappings through their life cycle and keeps a journal of what it did. */
data class NodeMappingService(
    var store: NodeMappingStore,
    var validator: NodeMappingValidator,
    var clock: Clock,
    var journal: MutableList<String>,
    var engine: NodeMappingEngine,
) {
    private fun note(message: String) {
        journal.add("${clock.now()} $message")
    }

    fun submit(code: String) {
        val item = store.find(code) ?: error("node mapping $code not found")
        validator.requireValid(item)
        item.moveTo(NodeMappingStatus.VALIDATED)
        store.save(item)
        note("$code is validated")
    }

    fun retire(code: String, reason: String) {
        val item = store.find(code) ?: error("node mapping $code not found")
        if (item.isTerminal()) {
            note("$code already closed")
            return
        }
        item.moveTo(NodeMappingStatus.RETIRED)
        store.save(item)
        note("$code is retired: $reason")
    }

    fun evaluate(code: String): Double {
        val item = store.find(code) ?: error("node mapping $code not found")
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

fun newNodeMappingService(store: NodeMappingStore, clock: Clock): NodeMappingService {
    return NodeMappingService(
        store = store,
        validator = defaultNodeMappingValidator(),
        clock = clock,
        journal = mutableListOf<String>(),
        engine = defaultNodeMappingEngine(),
    )
}
