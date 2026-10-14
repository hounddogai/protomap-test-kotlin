package com.acme.transport.network.hub.allocation

import com.acme.transport.network.clock.Clock

/** Moves hub allocations through their life cycle and keeps a journal of what it did. */
data class HubAllocationService(
    var store: HubAllocationStore,
    var validator: HubAllocationValidator,
    var clock: Clock,
    var journal: MutableList<String>,
) {
    private fun note(message: String) {
        journal.add("${clock.now()} $message")
    }

    fun advance(code: String) {
        val item = store.find(code) ?: error("hub allocation $code not found")
        validator.requireValid(item)
        item.moveTo(HubAllocationStatus.APPROVED)
        store.save(item)
        note("$code is approved")
    }

    fun complete(code: String, reason: String) {
        val item = store.find(code) ?: error("hub allocation $code not found")
        if (item.isTerminal()) {
            note("$code already closed")
            return
        }
        item.moveTo(HubAllocationStatus.WITHDRAWN)
        store.save(item)
        note("$code is withdrawn: $reason")
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

fun newHubAllocationService(store: HubAllocationStore, clock: Clock): HubAllocationService {
    return HubAllocationService(
        store = store,
        validator = defaultHubAllocationValidator(),
        clock = clock,
        journal = mutableListOf<String>(),
    )
}
