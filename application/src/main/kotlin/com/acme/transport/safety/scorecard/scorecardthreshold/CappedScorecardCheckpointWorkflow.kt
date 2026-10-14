package com.acme.transport.safety.scorecard.scorecardthreshold

import com.acme.transport.safety.clock.Clock

/** Moves capped scorecard checkpoints through their life cycle and keeps a journal of what it did. */
data class CappedScorecardCheckpointWorkflow(
    var store: CappedScorecardCheckpointStore,
    var validator: CappedScorecardCheckpointValidator,
    var clock: Clock,
    var journal: MutableList<String>,
) {
    private fun note(message: String) {
        journal.add("${clock.now()} $message")
    }

    fun submit(code: String) {
        val item = store.find(code) ?: error("capped scorecard checkpoint $code not found")
        validator.requireValid(item)
        item.moveTo(CappedScorecardCheckpointStatus.SCHEDULED)
        store.save(item)
        note("$code is scheduled")
    }

    fun retire(code: String, reason: String) {
        val item = store.find(code) ?: error("capped scorecard checkpoint $code not found")
        if (item.isTerminal()) {
            note("$code already closed")
            return
        }
        item.moveTo(CappedScorecardCheckpointStatus.CANCELLED)
        store.save(item)
        note("$code is cancelled: $reason")
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

fun newCappedScorecardCheckpointWorkflow(store: CappedScorecardCheckpointStore, clock: Clock): CappedScorecardCheckpointWorkflow {
    return CappedScorecardCheckpointWorkflow(
        store = store,
        validator = defaultCappedScorecardCheckpointValidator(),
        clock = clock,
        journal = mutableListOf<String>(),
    )
}
