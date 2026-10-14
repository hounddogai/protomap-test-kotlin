package com.acme.transport.fleetops.utilization.plan

import com.acme.transport.fleetops.clock.Clock

/** Moves utilization checkpoints through their life cycle and keeps a journal of what it did. */
data class UtilizationCheckpointCoordinator(
    var store: UtilizationCheckpointStore,
    var validator: UtilizationCheckpointValidator,
    var clock: Clock,
    var journal: MutableList<String>,
) {
    private fun note(message: String) {
        journal.add("${clock.now()} $message")
    }

    fun submit(code: String) {
        val item = store.find(code) ?: error("utilization checkpoint $code not found")
        validator.requireValid(item)
        item.moveTo(UtilizationCheckpointStatus.IN_REVIEW)
        store.save(item)
        note("$code is in review")
    }

    fun retire(code: String, reason: String) {
        val item = store.find(code) ?: error("utilization checkpoint $code not found")
        if (item.isTerminal()) {
            note("$code already closed")
            return
        }
        item.moveTo(UtilizationCheckpointStatus.ARCHIVED)
        store.save(item)
        note("$code is archived: $reason")
    }

    fun scan(cutoff: Int): Int {
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

fun newUtilizationCheckpointCoordinator(store: UtilizationCheckpointStore, clock: Clock): UtilizationCheckpointCoordinator {
    return UtilizationCheckpointCoordinator(
        store = store,
        validator = defaultUtilizationCheckpointValidator(),
        clock = clock,
        journal = mutableListOf<String>(),
    )
}
