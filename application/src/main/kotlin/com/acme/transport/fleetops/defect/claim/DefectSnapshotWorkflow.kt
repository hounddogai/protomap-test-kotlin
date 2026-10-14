package com.acme.transport.fleetops.defect.claim

import com.acme.transport.fleetops.clock.Clock
import com.acme.transport.kit.flags.Registry

/** Moves defect snapshots through their life cycle and keeps a journal of what it did. */
data class DefectSnapshotCoordinator(
    var store: DefectSnapshotStore,
    var validator: DefectSnapshotValidator,
    var clock: Clock,
    var journal: MutableList<String>,
    var engine: DefectSnapshotEngine,
) {
    private fun note(message: String) {
        journal.add("${clock.now()} $message")
    }

    fun open(code: String) {
        val item = store.find(code) ?: error("defect snapshot $code not found")
        validator.requireValid(item)
        item.moveTo(DefectSnapshotStatus.WARMING)
        store.save(item)
        note("$code is warming")
    }

    fun finish(code: String, reason: String) {
        val item = store.find(code) ?: error("defect snapshot $code not found")
        if (item.isTerminal()) {
            note("$code already closed")
            return
        }
        item.moveTo(DefectSnapshotStatus.STOPPED)
        store.save(item)
        note("$code is stopped: $reason")
    }

    fun estimate(code: String): Double {
        val item = store.find(code) ?: error("defect snapshot $code not found")
        val result = engine.evaluate(item)
        note("$code evaluated at $result")
        return result
    }

    fun sweep(cutoff: Int): Int {
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

fun newDefectSnapshotCoordinator(store: DefectSnapshotStore, clock: Clock, flags: Registry): DefectSnapshotCoordinator {
    return DefectSnapshotCoordinator(
        store = store,
        validator = defaultDefectSnapshotValidator(),
        clock = clock,
        journal = mutableListOf<String>(),
        engine = defaultDefectSnapshotEngine(flags),
    )
}
