package com.acme.transport.customs.broker.snapshot

import com.acme.transport.customs.clock.Clock
import com.acme.transport.kit.flags.Registry

/** Moves broker snapshots through their life cycle and keeps a journal of what it did. */
data class BrokerSnapshotService(
    var store: BrokerSnapshotStore,
    var validator: BrokerSnapshotValidator,
    var clock: Clock,
    var journal: MutableList<String>,
    var engine: BrokerSnapshotEngine,
) {
    private fun note(message: String) {
        journal.add("${clock.now()} $message")
    }

    fun open(code: String) {
        val item = store.find(code) ?: error("broker snapshot $code not found")
        validator.requireValid(item)
        item.moveTo(BrokerSnapshotStatus.WARMING)
        store.save(item)
        note("$code is warming")
    }

    fun close(code: String, reason: String) {
        val item = store.find(code) ?: error("broker snapshot $code not found")
        if (item.isTerminal()) {
            note("$code already closed")
            return
        }
        item.moveTo(BrokerSnapshotStatus.STOPPED)
        store.save(item)
        note("$code is stopped: $reason")
    }

    fun estimate(code: String): Double {
        val item = store.find(code) ?: error("broker snapshot $code not found")
        val result = engine.run(item)
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

fun newBrokerSnapshotService(store: BrokerSnapshotStore, clock: Clock, flags: Registry): BrokerSnapshotService {
    return BrokerSnapshotService(
        store = store,
        validator = defaultBrokerSnapshotValidator(),
        clock = clock,
        journal = mutableListOf<String>(),
        engine = defaultBrokerSnapshotEngine(flags),
    )
}
