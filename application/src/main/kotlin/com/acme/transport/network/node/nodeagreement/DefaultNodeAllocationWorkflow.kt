package com.acme.transport.network.node.nodeagreement

import com.acme.transport.network.clock.Clock

/** Moves default node allocations through their life cycle and keeps a journal of what it did. */
data class DefaultNodeAllocationManager(
    var store: DefaultNodeAllocationStore,
    var validator: DefaultNodeAllocationValidator,
    var clock: Clock,
    var journal: MutableList<String>,
) {
    private fun note(message: String) {
        journal.add("${clock.now()} $message")
    }

    fun open(code: String) {
        val item = store.find(code) ?: error("default node allocation $code not found")
        validator.requireValid(item)
        item.moveTo(DefaultNodeAllocationStatus.PENDING)
        store.save(item)
        note("$code is pending")
    }

    fun complete(code: String, reason: String) {
        val item = store.find(code) ?: error("default node allocation $code not found")
        if (item.isTerminal()) {
            note("$code already closed")
            return
        }
        item.moveTo(DefaultNodeAllocationStatus.CLOSED)
        store.save(item)
        note("$code is closed: $reason")
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

fun newDefaultNodeAllocationManager(store: DefaultNodeAllocationStore, clock: Clock): DefaultNodeAllocationManager {
    return DefaultNodeAllocationManager(
        store = store,
        validator = defaultDefaultNodeAllocationValidator(),
        clock = clock,
        journal = mutableListOf<String>(),
    )
}
