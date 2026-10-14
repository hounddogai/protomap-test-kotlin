package com.acme.transport.fleetops.defect.defectforecast

import com.acme.transport.fleetops.clock.Clock
import com.acme.transport.kit.flags.Registry

/** Moves legacy defect snapshots through their life cycle and keeps a journal of what it did. */
data class LegacyDefectSnapshotDesk(
    var store: LegacyDefectSnapshotStore,
    var validator: LegacyDefectSnapshotValidator,
    var clock: Clock,
    var journal: MutableList<String>,
    var engine: LegacyDefectSnapshotEngine,
) {
    private fun note(message: String) {
        journal.add("${clock.now()} $message")
    }

    fun open(code: String) {
        val item = store.find(code) ?: error("legacy defect snapshot $code not found")
        validator.requireValid(item)
        item.moveTo(LegacyDefectSnapshotStatus.SCHEDULED)
        store.save(item)
        note("$code is scheduled")
    }

    fun close(code: String, reason: String) {
        val item = store.find(code) ?: error("legacy defect snapshot $code not found")
        if (item.isTerminal()) {
            note("$code already closed")
            return
        }
        item.moveTo(LegacyDefectSnapshotStatus.CANCELLED)
        store.save(item)
        note("$code is cancelled: $reason")
    }

    fun evaluate(code: String): Double {
        val item = store.find(code) ?: error("legacy defect snapshot $code not found")
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

fun newLegacyDefectSnapshotDesk(store: LegacyDefectSnapshotStore, clock: Clock, flags: Registry): LegacyDefectSnapshotDesk {
    return LegacyDefectSnapshotDesk(
        store = store,
        validator = defaultLegacyDefectSnapshotValidator(),
        clock = clock,
        journal = mutableListOf<String>(),
        engine = defaultLegacyDefectSnapshotEngine(flags),
    )
}
