package com.acme.transport.network.node.forecast

import com.acme.transport.network.clock.Clock

/** Moves node forecasts through their life cycle and keeps a journal of what it did. */
data class NodeForecastService(
    var store: NodeForecastStore,
    var validator: NodeForecastValidator,
    var clock: Clock,
    var journal: MutableList<String>,
) {
    private fun note(message: String) {
        journal.add("${clock.now()} $message")
    }

    fun open(code: String) {
        val item = store.find(code) ?: error("node forecast $code not found")
        validator.requireValid(item)
        item.moveTo(NodeForecastStatus.PENDING)
        store.save(item)
        note("$code is pending")
    }

    fun retire(code: String, reason: String) {
        val item = store.find(code) ?: error("node forecast $code not found")
        if (item.isTerminal()) {
            note("$code already closed")
            return
        }
        item.moveTo(NodeForecastStatus.CLOSED)
        store.save(item)
        note("$code is closed: $reason")
    }

    fun audit(cutoff: Int): Int {
        var changed = 0
        for (item in store.list()) {
            if (item.isTerminal()) {
                continue
            }
            if (item.createdAt > cutoff) {
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

fun newNodeForecastService(store: NodeForecastStore, clock: Clock): NodeForecastService {
    return NodeForecastService(
        store = store,
        validator = defaultNodeForecastValidator(),
        clock = clock,
        journal = mutableListOf<String>(),
    )
}
