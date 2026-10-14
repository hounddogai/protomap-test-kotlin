package com.acme.transport.crewing.swap.checkpoint

import com.acme.transport.crewing.clock.Clock
import com.acme.transport.kit.flags.Registry

/** Moves strict swap checkpoints through their life cycle and keeps a journal of what it did. */
data class StrictSwapCheckpointCoordinator(
    var store: StrictSwapCheckpointStore,
    var validator: StrictSwapCheckpointValidator,
    var clock: Clock,
    var journal: MutableList<String>,
    var engine: StrictSwapCheckpointEngine,
) {
    private fun note(message: String) {
        journal.add("${clock.now()} $message")
    }

    fun start(code: String) {
        val item = store.find(code) ?: error("strict swap checkpoint $code not found")
        validator.requireValid(item)
        item.moveTo(StrictSwapCheckpointStatus.SCHEDULED)
        store.save(item)
        note("$code is scheduled")
    }

    fun settle(code: String, reason: String) {
        val item = store.find(code) ?: error("strict swap checkpoint $code not found")
        if (item.isTerminal()) {
            note("$code already closed")
            return
        }
        item.moveTo(StrictSwapCheckpointStatus.CANCELLED)
        store.save(item)
        note("$code is cancelled: $reason")
    }

    fun quote(code: String): Double {
        val item = store.find(code) ?: error("strict swap checkpoint $code not found")
        val result = engine.evaluate(item)
        note("$code evaluated at $result")
        return result
    }

    fun reviewStale(cutoff: Int): Int {
        var changed = 0
        for (item in store.list()) {
            if (item.isTerminal()) {
                continue
            }
            if (item.expiresAt > cutoff) {
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

fun newStrictSwapCheckpointCoordinator(store: StrictSwapCheckpointStore, clock: Clock, flags: Registry): StrictSwapCheckpointCoordinator {
    return StrictSwapCheckpointCoordinator(
        store = store,
        validator = defaultStrictSwapCheckpointValidator(),
        clock = clock,
        journal = mutableListOf<String>(),
        engine = defaultStrictSwapCheckpointEngine(flags),
    )
}
