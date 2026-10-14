package com.acme.transport.customs.broker.checkpoint

import com.acme.transport.customs.clock.Clock

/** Moves capped broker checkpoints through their life cycle and keeps a journal of what it did. */
data class CappedBrokerCheckpointManager(
    var store: CappedBrokerCheckpointStore,
    var validator: CappedBrokerCheckpointValidator,
    var clock: Clock,
    var journal: MutableList<String>,
) {
    private fun note(message: String) {
        journal.add("${clock.now()} $message")
    }

    fun submit(code: String) {
        val item = store.find(code) ?: error("capped broker checkpoint $code not found")
        validator.requireValid(item)
        item.moveTo(CappedBrokerCheckpointStatus.PENDING)
        store.save(item)
        note("$code is pending")
    }

    fun complete(code: String, reason: String) {
        val item = store.find(code) ?: error("capped broker checkpoint $code not found")
        if (item.isTerminal()) {
            note("$code already closed")
            return
        }
        item.moveTo(CappedBrokerCheckpointStatus.CLOSED)
        store.save(item)
        note("$code is closed: $reason")
    }

    fun reviewStale(cutoff: Int): Int {
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

fun newCappedBrokerCheckpointManager(store: CappedBrokerCheckpointStore, clock: Clock): CappedBrokerCheckpointManager {
    return CappedBrokerCheckpointManager(
        store = store,
        validator = defaultCappedBrokerCheckpointValidator(),
        clock = clock,
        journal = mutableListOf<String>(),
    )
}
