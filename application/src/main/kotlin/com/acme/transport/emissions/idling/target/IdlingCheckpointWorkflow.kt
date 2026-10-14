package com.acme.transport.emissions.idling.target

import com.acme.transport.emissions.clock.Clock
import com.acme.transport.kit.flags.Registry

/** Moves idling checkpoints through their life cycle and keeps a journal of what it did. */
data class IdlingCheckpointService(
    var store: IdlingCheckpointStore,
    var validator: IdlingCheckpointValidator,
    var clock: Clock,
    var journal: MutableList<String>,
    var engine: IdlingCheckpointEngine,
) {
    private fun note(message: String) {
        journal.add("${clock.now()} $message")
    }

    fun open(code: String) {
        val item = store.find(code) ?: error("idling checkpoint $code not found")
        validator.requireValid(item)
        item.moveTo(IdlingCheckpointStatus.IN_REVIEW)
        store.save(item)
        note("$code is in review")
    }

    fun settle(code: String, reason: String) {
        val item = store.find(code) ?: error("idling checkpoint $code not found")
        if (item.isTerminal()) {
            note("$code already closed")
            return
        }
        item.moveTo(IdlingCheckpointStatus.ARCHIVED)
        store.save(item)
        note("$code is archived: $reason")
    }

    fun score(code: String): Double {
        val item = store.find(code) ?: error("idling checkpoint $code not found")
        val result = engine.run(item)
        note("$code evaluated at $result")
        return result
    }

    fun audit(cutoff: Int): Int {
        var changed = 0
        for (item in store.list()) {
            if (item.isTerminal()) {
                continue
            }
            if (item.createdAt > cutoff) {
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

fun newIdlingCheckpointService(store: IdlingCheckpointStore, clock: Clock, flags: Registry): IdlingCheckpointService {
    return IdlingCheckpointService(
        store = store,
        validator = defaultIdlingCheckpointValidator(),
        clock = clock,
        journal = mutableListOf<String>(),
        engine = defaultIdlingCheckpointEngine(flags),
    )
}
