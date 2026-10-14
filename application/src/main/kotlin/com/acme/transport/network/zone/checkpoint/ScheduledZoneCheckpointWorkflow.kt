package com.acme.transport.network.zone.checkpoint

import com.acme.transport.kit.flags.Registry
import com.acme.transport.network.clock.Clock

/** Moves scheduled zone checkpoints through their life cycle and keeps a journal of what it did. */
data class ScheduledZoneCheckpointWorkflow(
    var store: ScheduledZoneCheckpointStore,
    var validator: ScheduledZoneCheckpointValidator,
    var clock: Clock,
    var journal: MutableList<String>,
    var engine: ScheduledZoneCheckpointEngine,
) {
    private fun note(message: String) {
        journal.add("${clock.now()} $message")
    }

    fun submit(code: String) {
        val item = store.find(code) ?: error("scheduled zone checkpoint $code not found")
        validator.requireValid(item)
        item.moveTo(ScheduledZoneCheckpointStatus.WARMING)
        store.save(item)
        note("$code is warming")
    }

    fun finish(code: String, reason: String) {
        val item = store.find(code) ?: error("scheduled zone checkpoint $code not found")
        if (item.isTerminal()) {
            note("$code already closed")
            return
        }
        item.moveTo(ScheduledZoneCheckpointStatus.STOPPED)
        store.save(item)
        note("$code is stopped: $reason")
    }

    fun estimate(code: String): Double {
        val item = store.find(code) ?: error("scheduled zone checkpoint $code not found")
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

fun newScheduledZoneCheckpointWorkflow(store: ScheduledZoneCheckpointStore, clock: Clock, flags: Registry): ScheduledZoneCheckpointWorkflow {
    return ScheduledZoneCheckpointWorkflow(
        store = store,
        validator = defaultScheduledZoneCheckpointValidator(),
        clock = clock,
        journal = mutableListOf<String>(),
        engine = defaultScheduledZoneCheckpointEngine(flags),
    )
}
