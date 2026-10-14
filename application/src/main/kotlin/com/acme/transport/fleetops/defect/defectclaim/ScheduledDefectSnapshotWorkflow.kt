package com.acme.transport.fleetops.defect.defectclaim

import com.acme.transport.fleetops.clock.Clock
import com.acme.transport.kit.flags.Registry

/** Moves scheduled defect snapshots through their life cycle and keeps a journal of what it did. */
data class ScheduledDefectSnapshotService(
    var store: ScheduledDefectSnapshotStore,
    var validator: ScheduledDefectSnapshotValidator,
    var clock: Clock,
    var journal: MutableList<String>,
    var engine: ScheduledDefectSnapshotEngine,
) {
    private fun note(message: String) {
        journal.add("${clock.now()} $message")
    }

    fun open(code: String) {
        val item = store.find(code) ?: error("scheduled defect snapshot $code not found")
        validator.requireValid(item)
        item.moveTo(ScheduledDefectSnapshotStatus.PENDING)
        store.save(item)
        note("$code is pending")
    }

    fun close(code: String, reason: String) {
        val item = store.find(code) ?: error("scheduled defect snapshot $code not found")
        if (item.isTerminal()) {
            note("$code already closed")
            return
        }
        item.moveTo(ScheduledDefectSnapshotStatus.CLOSED)
        store.save(item)
        note("$code is closed: $reason")
    }

    fun estimate(code: String): Double {
        val item = store.find(code) ?: error("scheduled defect snapshot $code not found")
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

fun newScheduledDefectSnapshotService(store: ScheduledDefectSnapshotStore, clock: Clock, flags: Registry): ScheduledDefectSnapshotService {
    return ScheduledDefectSnapshotService(
        store = store,
        validator = defaultScheduledDefectSnapshotValidator(),
        clock = clock,
        journal = mutableListOf<String>(),
        engine = defaultScheduledDefectSnapshotEngine(flags),
    )
}
