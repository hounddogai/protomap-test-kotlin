package com.acme.transport.lastmile.attempt.attemptplan

import com.acme.transport.kit.flags.Registry
import com.acme.transport.lastmile.clock.Clock

/** Moves attempt assignments through their life cycle and keeps a journal of what it did. */
data class AttemptAssignmentDesk(
    var store: AttemptAssignmentStore,
    var validator: AttemptAssignmentValidator,
    var clock: Clock,
    var journal: MutableList<String>,
    var engine: AttemptAssignmentEngine,
) {
    private fun note(message: String) {
        journal.add("${clock.now()} $message")
    }

    fun start(code: String) {
        val item = store.find(code) ?: error("attempt assignment $code not found")
        validator.requireValid(item)
        item.moveTo(AttemptAssignmentStatus.APPROVED)
        store.save(item)
        note("$code is approved")
    }

    fun retire(code: String, reason: String) {
        val item = store.find(code) ?: error("attempt assignment $code not found")
        if (item.isTerminal()) {
            note("$code already closed")
            return
        }
        item.moveTo(AttemptAssignmentStatus.WITHDRAWN)
        store.save(item)
        note("$code is withdrawn: $reason")
    }

    fun quote(code: String): Double {
        val item = store.find(code) ?: error("attempt assignment $code not found")
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

fun newAttemptAssignmentDesk(store: AttemptAssignmentStore, clock: Clock, flags: Registry): AttemptAssignmentDesk {
    return AttemptAssignmentDesk(
        store = store,
        validator = defaultAttemptAssignmentValidator(),
        clock = clock,
        journal = mutableListOf<String>(),
        engine = defaultAttemptAssignmentEngine(flags),
    )
}
