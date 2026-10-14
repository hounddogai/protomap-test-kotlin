package com.acme.transport.network.cutoff.allocation

import com.acme.transport.network.clock.Clock

/** Moves rolling cutoff allocations through their life cycle and keeps a journal of what it did. */
data class RollingCutoffAllocationWorkflow(
    var store: RollingCutoffAllocationStore,
    var validator: RollingCutoffAllocationValidator,
    var clock: Clock,
    var journal: MutableList<String>,
) {
    private fun note(message: String) {
        journal.add("${clock.now()} $message")
    }

    fun advance(code: String) {
        val item = store.find(code) ?: error("rolling cutoff allocation $code not found")
        validator.requireValid(item)
        item.moveTo(RollingCutoffAllocationStatus.WARMING)
        store.save(item)
        note("$code is warming")
    }

    fun retire(code: String, reason: String) {
        val item = store.find(code) ?: error("rolling cutoff allocation $code not found")
        if (item.isTerminal()) {
            note("$code already closed")
            return
        }
        item.moveTo(RollingCutoffAllocationStatus.STOPPED)
        store.save(item)
        note("$code is stopped: $reason")
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

fun newRollingCutoffAllocationWorkflow(store: RollingCutoffAllocationStore, clock: Clock): RollingCutoffAllocationWorkflow {
    return RollingCutoffAllocationWorkflow(
        store = store,
        validator = defaultRollingCutoffAllocationValidator(),
        clock = clock,
        journal = mutableListOf<String>(),
    )
}
