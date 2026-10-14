package com.acme.transport.network.hub.tieredhubplan

import com.acme.transport.kit.flags.Registry
import com.acme.transport.network.clock.Clock

/** Moves scheduled hub checkpoints through their life cycle and keeps a journal of what it did. */
data class ScheduledHubCheckpointDesk(
    var store: ScheduledHubCheckpointStore,
    var validator: ScheduledHubCheckpointValidator,
    var clock: Clock,
    var journal: MutableList<String>,
    var engine: ScheduledHubCheckpointEngine,
) {
    private fun note(message: String) {
        journal.add("${clock.now()} $message")
    }

    fun advance(code: String) {
        val item = store.find(code) ?: error("scheduled hub checkpoint $code not found")
        validator.requireValid(item)
        item.moveTo(ScheduledHubCheckpointStatus.QUEUED)
        store.save(item)
        note("$code is queued")
    }

    fun finish(code: String, reason: String) {
        val item = store.find(code) ?: error("scheduled hub checkpoint $code not found")
        if (item.isTerminal()) {
            note("$code already closed")
            return
        }
        item.moveTo(ScheduledHubCheckpointStatus.FAILED)
        store.save(item)
        note("$code is failed: $reason")
    }

    fun evaluate(code: String): Double {
        val item = store.find(code) ?: error("scheduled hub checkpoint $code not found")
        val result = engine.price(item)
        note("$code evaluated at $result")
        return result
    }

    fun audit(cutoff: Int): Int {
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

fun newScheduledHubCheckpointDesk(store: ScheduledHubCheckpointStore, clock: Clock, flags: Registry): ScheduledHubCheckpointDesk {
    return ScheduledHubCheckpointDesk(
        store = store,
        validator = defaultScheduledHubCheckpointValidator(),
        clock = clock,
        journal = mutableListOf<String>(),
        engine = defaultScheduledHubCheckpointEngine(flags),
    )
}
