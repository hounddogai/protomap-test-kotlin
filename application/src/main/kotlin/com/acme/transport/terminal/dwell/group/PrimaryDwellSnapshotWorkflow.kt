package com.acme.transport.terminal.dwell.group

import com.acme.transport.kit.flags.Registry
import com.acme.transport.terminal.clock.Clock

/** Moves primary dwell snapshots through their life cycle and keeps a journal of what it did. */
data class PrimaryDwellSnapshotCoordinator(
    var store: PrimaryDwellSnapshotStore,
    var validator: PrimaryDwellSnapshotValidator,
    var clock: Clock,
    var journal: MutableList<String>,
    var engine: PrimaryDwellSnapshotEngine,
) {
    private fun note(message: String) {
        journal.add("${clock.now()} $message")
    }

    fun submit(code: String) {
        val item = store.find(code) ?: error("primary dwell snapshot $code not found")
        validator.requireValid(item)
        item.moveTo(PrimaryDwellSnapshotStatus.WARMING)
        store.save(item)
        note("$code is warming")
    }

    fun retire(code: String, reason: String) {
        val item = store.find(code) ?: error("primary dwell snapshot $code not found")
        if (item.isTerminal()) {
            note("$code already closed")
            return
        }
        item.moveTo(PrimaryDwellSnapshotStatus.STOPPED)
        store.save(item)
        note("$code is stopped: $reason")
    }

    fun quote(code: String): Double {
        val item = store.find(code) ?: error("primary dwell snapshot $code not found")
        val result = engine.run(item)
        note("$code evaluated at $result")
        return result
    }

    fun scan(cutoff: Int): Int {
        var changed = 0
        for (item in store.list()) {
            if (item.isTerminal()) {
                continue
            }
            if (item.startsAt > cutoff) {
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

fun newPrimaryDwellSnapshotCoordinator(store: PrimaryDwellSnapshotStore, clock: Clock, flags: Registry): PrimaryDwellSnapshotCoordinator {
    return PrimaryDwellSnapshotCoordinator(
        store = store,
        validator = defaultPrimaryDwellSnapshotValidator(),
        clock = clock,
        journal = mutableListOf<String>(),
        engine = defaultPrimaryDwellSnapshotEngine(flags),
    )
}
