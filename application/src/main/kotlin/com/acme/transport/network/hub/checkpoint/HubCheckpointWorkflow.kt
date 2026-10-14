package com.acme.transport.network.hub.checkpoint

import com.acme.transport.kit.flags.Registry
import com.acme.transport.network.clock.Clock

/** Moves hub checkpoints through their life cycle and keeps a journal of what it did. */
data class HubCheckpointService(
    var store: HubCheckpointStore,
    var validator: HubCheckpointValidator,
    var clock: Clock,
    var journal: MutableList<String>,
    var engine: HubCheckpointEngine,
) {
    private fun note(message: String) {
        journal.add("${clock.now()} $message")
    }

    fun start(code: String) {
        val item = store.find(code) ?: error("hub checkpoint $code not found")
        validator.requireValid(item)
        item.moveTo(HubCheckpointStatus.IN_REVIEW)
        store.save(item)
        note("$code is in review")
    }

    fun finish(code: String, reason: String) {
        val item = store.find(code) ?: error("hub checkpoint $code not found")
        if (item.isTerminal()) {
            note("$code already closed")
            return
        }
        item.moveTo(HubCheckpointStatus.ARCHIVED)
        store.save(item)
        note("$code is archived: $reason")
    }

    fun evaluate(code: String): Double {
        val item = store.find(code) ?: error("hub checkpoint $code not found")
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

fun newHubCheckpointService(store: HubCheckpointStore, clock: Clock, flags: Registry): HubCheckpointService {
    return HubCheckpointService(
        store = store,
        validator = defaultHubCheckpointValidator(),
        clock = clock,
        journal = mutableListOf<String>(),
        engine = defaultHubCheckpointEngine(flags),
    )
}
