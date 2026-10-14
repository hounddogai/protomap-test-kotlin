package com.acme.transport.safety.incident.incidentoverride

import com.acme.transport.safety.clock.Clock

/** Moves regional incident snapshots through their life cycle and keeps a journal of what it did. */
data class RegionalIncidentSnapshotService(
    var store: RegionalIncidentSnapshotStore,
    var validator: RegionalIncidentSnapshotValidator,
    var clock: Clock,
    var journal: MutableList<String>,
) {
    private fun note(message: String) {
        journal.add("${clock.now()} $message")
    }

    fun advance(code: String) {
        val item = store.find(code) ?: error("regional incident snapshot $code not found")
        validator.requireValid(item)
        item.moveTo(RegionalIncidentSnapshotStatus.IN_REVIEW)
        store.save(item)
        note("$code is in review")
    }

    fun finish(code: String, reason: String) {
        val item = store.find(code) ?: error("regional incident snapshot $code not found")
        if (item.isTerminal()) {
            note("$code already closed")
            return
        }
        item.moveTo(RegionalIncidentSnapshotStatus.ARCHIVED)
        store.save(item)
        note("$code is archived: $reason")
    }

    fun sweep(cutoff: Int): Int {
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

fun newRegionalIncidentSnapshotService(store: RegionalIncidentSnapshotStore, clock: Clock): RegionalIncidentSnapshotService {
    return RegionalIncidentSnapshotService(
        store = store,
        validator = defaultRegionalIncidentSnapshotValidator(),
        clock = clock,
        journal = mutableListOf<String>(),
    )
}
