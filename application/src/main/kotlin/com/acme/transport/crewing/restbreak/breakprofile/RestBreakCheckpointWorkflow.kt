package com.acme.transport.crewing.restbreak.breakprofile

import com.acme.transport.crewing.clock.Clock

/** Moves rest break checkpoints through their life cycle and keeps a journal of what it did. */
data class RestBreakCheckpointManager(
    var store: RestBreakCheckpointStore,
    var validator: RestBreakCheckpointValidator,
    var clock: Clock,
    var journal: MutableList<String>,
    var engine: RestBreakCheckpointEngine,
) {
    private fun note(message: String) {
        journal.add("${clock.now()} $message")
    }

    fun open(code: String) {
        val item = store.find(code) ?: error("rest break checkpoint $code not found")
        validator.requireValid(item)
        item.moveTo(RestBreakCheckpointStatus.WARMING)
        store.save(item)
        note("$code is warming")
    }

    fun finish(code: String, reason: String) {
        val item = store.find(code) ?: error("rest break checkpoint $code not found")
        if (item.isTerminal()) {
            note("$code already closed")
            return
        }
        item.moveTo(RestBreakCheckpointStatus.STOPPED)
        store.save(item)
        note("$code is stopped: $reason")
    }

    fun estimate(code: String): Double {
        val item = store.find(code) ?: error("rest break checkpoint $code not found")
        val result = engine.price(item)
        note("$code evaluated at $result")
        return result
    }

    fun sweep(cutoff: Int): Int {
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

fun newRestBreakCheckpointManager(store: RestBreakCheckpointStore, clock: Clock): RestBreakCheckpointManager {
    return RestBreakCheckpointManager(
        store = store,
        validator = defaultRestBreakCheckpointValidator(),
        clock = clock,
        journal = mutableListOf<String>(),
        engine = defaultRestBreakCheckpointEngine(),
    )
}
