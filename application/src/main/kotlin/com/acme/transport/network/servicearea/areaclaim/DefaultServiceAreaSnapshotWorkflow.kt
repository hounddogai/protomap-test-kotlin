package com.acme.transport.network.servicearea.areaclaim

import com.acme.transport.kit.flags.Registry
import com.acme.transport.network.clock.Clock

/**
 * Moves default service area snapshots through their life cycle and keeps a journal of what it did.
 */
data class DefaultServiceAreaSnapshotCoordinator(
    var store: DefaultServiceAreaSnapshotStore,
    var validator: DefaultServiceAreaSnapshotValidator,
    var clock: Clock,
    var journal: MutableList<String>,
    var engine: DefaultServiceAreaSnapshotEngine,
) {
    private fun note(message: String) {
        journal.add("${clock.now()} $message")
    }

    fun open(code: String) {
        val item = store.find(code) ?: error("default service area snapshot $code not found")
        validator.requireValid(item)
        item.moveTo(DefaultServiceAreaSnapshotStatus.PENDING)
        store.save(item)
        note("$code is pending")
    }

    fun retire(code: String, reason: String) {
        val item = store.find(code) ?: error("default service area snapshot $code not found")
        if (item.isTerminal()) {
            note("$code already closed")
            return
        }
        item.moveTo(DefaultServiceAreaSnapshotStatus.CLOSED)
        store.save(item)
        note("$code is closed: $reason")
    }

    fun estimate(code: String): Double {
        val item = store.find(code) ?: error("default service area snapshot $code not found")
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

fun newDefaultServiceAreaSnapshotCoordinator(store: DefaultServiceAreaSnapshotStore, clock: Clock, flags: Registry): DefaultServiceAreaSnapshotCoordinator {
    return DefaultServiceAreaSnapshotCoordinator(
        store = store,
        validator = defaultDefaultServiceAreaSnapshotValidator(),
        clock = clock,
        journal = mutableListOf<String>(),
        engine = defaultDefaultServiceAreaSnapshotEngine(flags),
    )
}
