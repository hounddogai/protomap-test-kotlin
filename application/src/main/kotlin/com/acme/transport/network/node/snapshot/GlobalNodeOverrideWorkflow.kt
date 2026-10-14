package com.acme.transport.network.node.snapshot

import com.acme.transport.network.clock.Clock

/** Moves global node overrides through their life cycle and keeps a journal of what it did. */
data class GlobalNodeOverrideCoordinator(
    var store: GlobalNodeOverrideStore,
    var validator: GlobalNodeOverrideValidator,
    var clock: Clock,
    var journal: MutableList<String>,
) {
    private fun note(message: String) {
        journal.add("${clock.now()} $message")
    }

    fun start(code: String) {
        val item = store.find(code) ?: error("global node override $code not found")
        validator.requireValid(item)
        item.moveTo(GlobalNodeOverrideStatus.SCHEDULED)
        store.save(item)
        note("$code is scheduled")
    }

    fun finish(code: String, reason: String) {
        val item = store.find(code) ?: error("global node override $code not found")
        if (item.isTerminal()) {
            note("$code already closed")
            return
        }
        item.moveTo(GlobalNodeOverrideStatus.CANCELLED)
        store.save(item)
        note("$code is cancelled: $reason")
    }

    fun scan(cutoff: Int): Int {
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

fun newGlobalNodeOverrideCoordinator(store: GlobalNodeOverrideStore, clock: Clock): GlobalNodeOverrideCoordinator {
    return GlobalNodeOverrideCoordinator(
        store = store,
        validator = defaultGlobalNodeOverrideValidator(),
        clock = clock,
        journal = mutableListOf<String>(),
    )
}
