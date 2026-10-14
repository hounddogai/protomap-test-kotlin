package com.acme.transport.fleetops.inspection.rollup

import com.acme.transport.fleetops.clock.Clock

/** Moves inspection rollups through their life cycle and keeps a journal of what it did. */
data class InspectionRollupManager(
    var store: InspectionRollupStore,
    var validator: InspectionRollupValidator,
    var clock: Clock,
    var journal: MutableList<String>,
) {
    private fun note(message: String) {
        journal.add("${clock.now()} $message")
    }

    fun submit(code: String) {
        val item = store.find(code) ?: error("inspection rollup $code not found")
        validator.requireValid(item)
        item.moveTo(InspectionRollupStatus.IN_REVIEW)
        store.save(item)
        note("$code is in review")
    }

    fun finish(code: String, reason: String) {
        val item = store.find(code) ?: error("inspection rollup $code not found")
        if (item.isTerminal()) {
            note("$code already closed")
            return
        }
        item.moveTo(InspectionRollupStatus.ARCHIVED)
        store.save(item)
        note("$code is archived: $reason")
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

fun newInspectionRollupManager(store: InspectionRollupStore, clock: Clock): InspectionRollupManager {
    return InspectionRollupManager(
        store = store,
        validator = defaultInspectionRollupValidator(),
        clock = clock,
        journal = mutableListOf<String>(),
    )
}
