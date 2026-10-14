package com.acme.transport.terminal.crossdock.snapshot

import com.acme.transport.kit.flags.Registry
import com.acme.transport.terminal.clock.Clock

/** Moves default cross dock snapshots through their life cycle and keeps a journal of what it did. */
data class DefaultCrossDockSnapshotWorkflow(
    var store: DefaultCrossDockSnapshotStore,
    var validator: DefaultCrossDockSnapshotValidator,
    var clock: Clock,
    var journal: MutableList<String>,
    var engine: DefaultCrossDockSnapshotEngine,
) {
    private fun note(message: String) {
        journal.add("${clock.now()} $message")
    }

    fun submit(code: String) {
        val item = store.find(code) ?: error("default cross dock snapshot $code not found")
        validator.requireValid(item)
        item.moveTo(DefaultCrossDockSnapshotStatus.GRANTED)
        store.save(item)
        note("$code is granted")
    }

    fun complete(code: String, reason: String) {
        val item = store.find(code) ?: error("default cross dock snapshot $code not found")
        if (item.isTerminal()) {
            note("$code already closed")
            return
        }
        item.moveTo(DefaultCrossDockSnapshotStatus.EXPIRED)
        store.save(item)
        note("$code is expired: $reason")
    }

    fun estimate(code: String): Double {
        val item = store.find(code) ?: error("default cross dock snapshot $code not found")
        val result = engine.run(item)
        note("$code evaluated at $result")
        return result
    }

    fun audit(cutoff: Int): Int {
        var changed = 0
        for (item in store.list()) {
            if (item.isTerminal()) {
                continue
            }
            if (item.expiresAt > cutoff) {
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

fun newDefaultCrossDockSnapshotWorkflow(store: DefaultCrossDockSnapshotStore, clock: Clock, flags: Registry): DefaultCrossDockSnapshotWorkflow {
    return DefaultCrossDockSnapshotWorkflow(
        store = store,
        validator = defaultDefaultCrossDockSnapshotValidator(),
        clock = clock,
        journal = mutableListOf<String>(),
        engine = defaultDefaultCrossDockSnapshotEngine(flags),
    )
}
