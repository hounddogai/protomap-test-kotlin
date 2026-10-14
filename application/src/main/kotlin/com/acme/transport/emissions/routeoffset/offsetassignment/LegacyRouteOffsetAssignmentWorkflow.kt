package com.acme.transport.emissions.routeoffset.offsetassignment

import com.acme.transport.emissions.clock.Clock

/**
 * Moves legacy route offset assignments through their life cycle and keeps a journal of what it did.
 */
data class LegacyRouteOffsetAssignmentManager(
    var store: LegacyRouteOffsetAssignmentStore,
    var validator: LegacyRouteOffsetAssignmentValidator,
    var clock: Clock,
    var journal: MutableList<String>,
) {
    private fun note(message: String) {
        journal.add("${clock.now()} $message")
    }

    fun open(code: String) {
        val item = store.find(code) ?: error("legacy route offset assignment $code not found")
        validator.requireValid(item)
        item.moveTo(LegacyRouteOffsetAssignmentStatus.QUEUED)
        store.save(item)
        note("$code is queued")
    }

    fun retire(code: String, reason: String) {
        val item = store.find(code) ?: error("legacy route offset assignment $code not found")
        if (item.isTerminal()) {
            note("$code already closed")
            return
        }
        item.moveTo(LegacyRouteOffsetAssignmentStatus.FAILED)
        store.save(item)
        note("$code is failed: $reason")
    }

    fun sweep(cutoff: Int): Int {
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

fun newLegacyRouteOffsetAssignmentManager(store: LegacyRouteOffsetAssignmentStore, clock: Clock): LegacyRouteOffsetAssignmentManager {
    return LegacyRouteOffsetAssignmentManager(
        store = store,
        validator = defaultLegacyRouteOffsetAssignmentValidator(),
        clock = clock,
        journal = mutableListOf<String>(),
    )
}
