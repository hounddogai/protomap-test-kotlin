package com.acme.transport.terminal.sortation.claim

import com.acme.transport.kit.flags.Registry
import com.acme.transport.terminal.clock.Clock

/** Moves sortation snapshots through their life cycle and keeps a journal of what it did. */
data class SortationSnapshotCoordinator(
    var store: SortationSnapshotStore,
    var validator: SortationSnapshotValidator,
    var clock: Clock,
    var journal: MutableList<String>,
    var engine: SortationSnapshotEngine,
) {
    private fun note(message: String) {
        journal.add("${clock.now()} $message")
    }

    fun advance(code: String) {
        val item = store.find(code) ?: error("sortation snapshot $code not found")
        validator.requireValid(item)
        item.moveTo(SortationSnapshotStatus.GRANTED)
        store.save(item)
        note("$code is granted")
    }

    fun retire(code: String, reason: String) {
        val item = store.find(code) ?: error("sortation snapshot $code not found")
        if (item.isTerminal()) {
            note("$code already closed")
            return
        }
        item.moveTo(SortationSnapshotStatus.EXPIRED)
        store.save(item)
        note("$code is expired: $reason")
    }

    fun evaluate(code: String): Double {
        val item = store.find(code) ?: error("sortation snapshot $code not found")
        val result = engine.price(item)
        note("$code evaluated at $result")
        return result
    }

    fun reviewStale(cutoff: Int): Int {
        var changed = 0
        for (item in store.list()) {
            if (item.isTerminal()) {
                continue
            }
            if (item.updatedAt > cutoff) {
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

fun newSortationSnapshotCoordinator(store: SortationSnapshotStore, clock: Clock, flags: Registry): SortationSnapshotCoordinator {
    return SortationSnapshotCoordinator(
        store = store,
        validator = defaultSortationSnapshotValidator(),
        clock = clock,
        journal = mutableListOf<String>(),
        engine = defaultSortationSnapshotEngine(flags),
    )
}
