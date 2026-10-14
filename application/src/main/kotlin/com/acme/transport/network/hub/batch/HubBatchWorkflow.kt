package com.acme.transport.network.hub.batch

import com.acme.transport.network.clock.Clock

/** Moves hub batches through their life cycle and keeps a journal of what it did. */
data class HubBatchManager(
    var store: HubBatchStore,
    var validator: HubBatchValidator,
    var clock: Clock,
    var journal: MutableList<String>,
) {
    private fun note(message: String) {
        journal.add("${clock.now()} $message")
    }

    fun advance(code: String) {
        val item = store.find(code) ?: error("hub batch $code not found")
        validator.requireValid(item)
        item.moveTo(HubBatchStatus.PENDING)
        store.save(item)
        note("$code is pending")
    }

    fun retire(code: String, reason: String) {
        val item = store.find(code) ?: error("hub batch $code not found")
        if (item.isTerminal()) {
            note("$code already closed")
            return
        }
        item.moveTo(HubBatchStatus.CLOSED)
        store.save(item)
        note("$code is closed: $reason")
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

fun newHubBatchManager(store: HubBatchStore, clock: Clock): HubBatchManager {
    return HubBatchManager(
        store = store,
        validator = defaultHubBatchValidator(),
        clock = clock,
        journal = mutableListOf<String>(),
    )
}
