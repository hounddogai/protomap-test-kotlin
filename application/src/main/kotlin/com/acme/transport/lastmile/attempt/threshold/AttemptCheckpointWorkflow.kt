package com.acme.transport.lastmile.attempt.threshold

import com.acme.transport.kit.flags.Registry
import com.acme.transport.lastmile.clock.Clock

/** Moves attempt checkpoints through their life cycle and keeps a journal of what it did. */
data class AttemptCheckpointService(
    var store: AttemptCheckpointStore,
    var validator: AttemptCheckpointValidator,
    var clock: Clock,
    var journal: MutableList<String>,
    var engine: AttemptCheckpointEngine,
) {
    private fun note(message: String) {
        journal.add("${clock.now()} $message")
    }

    fun start(code: String) {
        val item = store.find(code) ?: error("attempt checkpoint $code not found")
        validator.requireValid(item)
        item.moveTo(AttemptCheckpointStatus.GRANTED)
        store.save(item)
        note("$code is granted")
    }

    fun finish(code: String, reason: String) {
        val item = store.find(code) ?: error("attempt checkpoint $code not found")
        if (item.isTerminal()) {
            note("$code already closed")
            return
        }
        item.moveTo(AttemptCheckpointStatus.EXPIRED)
        store.save(item)
        note("$code is expired: $reason")
    }

    fun quote(code: String): Double {
        val item = store.find(code) ?: error("attempt checkpoint $code not found")
        val result = engine.compute(item)
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

fun newAttemptCheckpointService(store: AttemptCheckpointStore, clock: Clock, flags: Registry): AttemptCheckpointService {
    return AttemptCheckpointService(
        store = store,
        validator = defaultAttemptCheckpointValidator(),
        clock = clock,
        journal = mutableListOf<String>(),
        engine = defaultAttemptCheckpointEngine(flags),
    )
}
