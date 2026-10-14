package com.acme.transport.network.transittime.timerequest

import com.acme.transport.network.clock.Clock

/**
 * Moves default transit time checkpoints through their life cycle and keeps a journal of what it did.
 */
data class DefaultTransitTimeCheckpointCoordinator(
    var store: DefaultTransitTimeCheckpointStore,
    var validator: DefaultTransitTimeCheckpointValidator,
    var clock: Clock,
    var journal: MutableList<String>,
    var engine: DefaultTransitTimeCheckpointEngine,
) {
    private fun note(message: String) {
        journal.add("${clock.now()} $message")
    }

    fun submit(code: String) {
        val item = store.find(code) ?: error("default transit time checkpoint $code not found")
        validator.requireValid(item)
        item.moveTo(DefaultTransitTimeCheckpointStatus.WARMING)
        store.save(item)
        note("$code is warming")
    }

    fun complete(code: String, reason: String) {
        val item = store.find(code) ?: error("default transit time checkpoint $code not found")
        if (item.isTerminal()) {
            note("$code already closed")
            return
        }
        item.moveTo(DefaultTransitTimeCheckpointStatus.STOPPED)
        store.save(item)
        note("$code is stopped: $reason")
    }

    fun score(code: String): Double {
        val item = store.find(code) ?: error("default transit time checkpoint $code not found")
        val result = engine.run(item)
        note("$code evaluated at $result")
        return result
    }

    fun sweep(cutoff: Int): Int {
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

fun newDefaultTransitTimeCheckpointCoordinator(store: DefaultTransitTimeCheckpointStore, clock: Clock): DefaultTransitTimeCheckpointCoordinator {
    return DefaultTransitTimeCheckpointCoordinator(
        store = store,
        validator = defaultDefaultTransitTimeCheckpointValidator(),
        clock = clock,
        journal = mutableListOf<String>(),
        engine = defaultDefaultTransitTimeCheckpointEngine(),
    )
}
