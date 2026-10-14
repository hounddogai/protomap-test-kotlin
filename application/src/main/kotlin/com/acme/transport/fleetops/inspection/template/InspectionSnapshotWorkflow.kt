package com.acme.transport.fleetops.inspection.template

import com.acme.transport.fleetops.clock.Clock

/** Moves inspection snapshots through their life cycle and keeps a journal of what it did. */
data class InspectionSnapshotService(
    var store: InspectionSnapshotStore,
    var validator: InspectionSnapshotValidator,
    var clock: Clock,
    var journal: MutableList<String>,
    var engine: InspectionSnapshotEngine,
) {
    private fun note(message: String) {
        journal.add("${clock.now()} $message")
    }

    fun open(code: String) {
        val item = store.find(code) ?: error("inspection snapshot $code not found")
        validator.requireValid(item)
        item.moveTo(InspectionSnapshotStatus.IN_REVIEW)
        store.save(item)
        note("$code is in review")
    }

    fun retire(code: String, reason: String) {
        val item = store.find(code) ?: error("inspection snapshot $code not found")
        if (item.isTerminal()) {
            note("$code already closed")
            return
        }
        item.moveTo(InspectionSnapshotStatus.ARCHIVED)
        store.save(item)
        note("$code is archived: $reason")
    }

    fun score(code: String): Double {
        val item = store.find(code) ?: error("inspection snapshot $code not found")
        val result = engine.run(item)
        note("$code evaluated at $result")
        return result
    }

    fun sweep(cutoff: Int): Int {
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

fun newInspectionSnapshotService(store: InspectionSnapshotStore, clock: Clock): InspectionSnapshotService {
    return InspectionSnapshotService(
        store = store,
        validator = defaultInspectionSnapshotValidator(),
        clock = clock,
        journal = mutableListOf<String>(),
        engine = defaultInspectionSnapshotEngine(),
    )
}
