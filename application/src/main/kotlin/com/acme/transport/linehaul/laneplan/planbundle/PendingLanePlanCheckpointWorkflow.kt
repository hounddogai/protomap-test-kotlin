package com.acme.transport.linehaul.laneplan.planbundle

import com.acme.transport.linehaul.clock.Clock

/** Moves pending lane plan checkpoints through their life cycle and keeps a journal of what it did. */
data class PendingLanePlanCheckpointWorkflow(
    var store: PendingLanePlanCheckpointStore,
    var validator: PendingLanePlanCheckpointValidator,
    var clock: Clock,
    var journal: MutableList<String>,
    var engine: PendingLanePlanCheckpointEngine,
) {
    private fun note(message: String) {
        journal.add("${clock.now()} $message")
    }

    fun advance(code: String) {
        val item = store.find(code) ?: error("pending lane plan checkpoint $code not found")
        validator.requireValid(item)
        item.moveTo(PendingLanePlanCheckpointStatus.IN_REVIEW)
        store.save(item)
        note("$code is in review")
    }

    fun close(code: String, reason: String) {
        val item = store.find(code) ?: error("pending lane plan checkpoint $code not found")
        if (item.isTerminal()) {
            note("$code already closed")
            return
        }
        item.moveTo(PendingLanePlanCheckpointStatus.ARCHIVED)
        store.save(item)
        note("$code is archived: $reason")
    }

    fun estimate(code: String): Double {
        val item = store.find(code) ?: error("pending lane plan checkpoint $code not found")
        val result = engine.run(item)
        note("$code evaluated at $result")
        return result
    }

    fun reviewStale(cutoff: Int): Int {
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

fun newPendingLanePlanCheckpointWorkflow(store: PendingLanePlanCheckpointStore, clock: Clock): PendingLanePlanCheckpointWorkflow {
    return PendingLanePlanCheckpointWorkflow(
        store = store,
        validator = defaultPendingLanePlanCheckpointValidator(),
        clock = clock,
        journal = mutableListOf<String>(),
        engine = defaultPendingLanePlanCheckpointEngine(),
    )
}
