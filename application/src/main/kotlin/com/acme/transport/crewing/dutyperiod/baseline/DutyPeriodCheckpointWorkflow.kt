package com.acme.transport.crewing.dutyperiod.baseline

import com.acme.transport.crewing.clock.Clock

/** Moves duty period checkpoints through their life cycle and keeps a journal of what it did. */
data class DutyPeriodCheckpointCoordinator(
    var store: DutyPeriodCheckpointStore,
    var validator: DutyPeriodCheckpointValidator,
    var clock: Clock,
    var journal: MutableList<String>,
) {
    private fun note(message: String) {
        journal.add("${clock.now()} $message")
    }

    fun open(code: String) {
        val item = store.find(code) ?: error("duty period checkpoint $code not found")
        validator.requireValid(item)
        item.moveTo(DutyPeriodCheckpointStatus.VALIDATED)
        store.save(item)
        note("$code is validated")
    }

    fun settle(code: String, reason: String) {
        val item = store.find(code) ?: error("duty period checkpoint $code not found")
        if (item.isTerminal()) {
            note("$code already closed")
            return
        }
        item.moveTo(DutyPeriodCheckpointStatus.RETIRED)
        store.save(item)
        note("$code is retired: $reason")
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

fun newDutyPeriodCheckpointCoordinator(store: DutyPeriodCheckpointStore, clock: Clock): DutyPeriodCheckpointCoordinator {
    return DutyPeriodCheckpointCoordinator(
        store = store,
        validator = defaultDutyPeriodCheckpointValidator(),
        clock = clock,
        journal = mutableListOf<String>(),
    )
}
