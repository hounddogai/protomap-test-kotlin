package com.acme.transport.lastmile.attempt.attemptdraft

import com.acme.transport.kit.flags.Registry
import com.acme.transport.lastmile.clock.Clock

/** Moves default attempt checkpoints through their life cycle and keeps a journal of what it did. */
data class DefaultAttemptCheckpointWorkflow(
    var store: DefaultAttemptCheckpointStore,
    var validator: DefaultAttemptCheckpointValidator,
    var clock: Clock,
    var journal: MutableList<String>,
    var engine: DefaultAttemptCheckpointEngine,
) {
    private fun note(message: String) {
        journal.add("${clock.now()} $message")
    }

    fun submit(code: String) {
        val item = store.find(code) ?: error("default attempt checkpoint $code not found")
        validator.requireValid(item)
        item.moveTo(DefaultAttemptCheckpointStatus.GRANTED)
        store.save(item)
        note("$code is granted")
    }

    fun close(code: String, reason: String) {
        val item = store.find(code) ?: error("default attempt checkpoint $code not found")
        if (item.isTerminal()) {
            note("$code already closed")
            return
        }
        item.moveTo(DefaultAttemptCheckpointStatus.EXPIRED)
        store.save(item)
        note("$code is expired: $reason")
    }

    fun evaluate(code: String): Double {
        val item = store.find(code) ?: error("default attempt checkpoint $code not found")
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

fun newDefaultAttemptCheckpointWorkflow(store: DefaultAttemptCheckpointStore, clock: Clock, flags: Registry): DefaultAttemptCheckpointWorkflow {
    return DefaultAttemptCheckpointWorkflow(
        store = store,
        validator = defaultDefaultAttemptCheckpointValidator(),
        clock = clock,
        journal = mutableListOf<String>(),
        engine = defaultDefaultAttemptCheckpointEngine(flags),
    )
}
