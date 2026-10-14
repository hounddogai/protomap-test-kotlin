package com.acme.transport.network.hub.mapping

import com.acme.transport.network.clock.Clock

/** Moves hub entries through their life cycle and keeps a journal of what it did. */
data class HubEntryDesk(
    var store: HubEntryStore,
    var validator: HubEntryValidator,
    var clock: Clock,
    var journal: MutableList<String>,
) {
    private fun note(message: String) {
        journal.add("${clock.now()} $message")
    }

    fun advance(code: String) {
        val item = store.find(code) ?: error("hub entry $code not found")
        validator.requireValid(item)
        item.moveTo(HubEntryStatus.QUEUED)
        store.save(item)
        note("$code is queued")
    }

    fun close(code: String, reason: String) {
        val item = store.find(code) ?: error("hub entry $code not found")
        if (item.isTerminal()) {
            note("$code already closed")
            return
        }
        item.moveTo(HubEntryStatus.FAILED)
        store.save(item)
        note("$code is failed: $reason")
    }

    fun audit(cutoff: Int): Int {
        var changed = 0
        for (item in store.list()) {
            if (item.isTerminal()) {
                continue
            }
            if (item.endsAt > cutoff) {
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

fun newHubEntryDesk(store: HubEntryStore, clock: Clock): HubEntryDesk {
    return HubEntryDesk(
        store = store,
        validator = defaultHubEntryValidator(),
        clock = clock,
        journal = mutableListOf<String>(),
    )
}
