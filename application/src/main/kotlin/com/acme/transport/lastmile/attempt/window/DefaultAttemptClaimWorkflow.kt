package com.acme.transport.lastmile.attempt.window

import com.acme.transport.kit.flags.Registry
import com.acme.transport.lastmile.clock.Clock

/** Moves default attempt claims through their life cycle and keeps a journal of what it did. */
data class DefaultAttemptClaimManager(
    var store: DefaultAttemptClaimStore,
    var validator: DefaultAttemptClaimValidator,
    var clock: Clock,
    var journal: MutableList<String>,
    var engine: DefaultAttemptClaimEngine,
) {
    private fun note(message: String) {
        journal.add("${clock.now()} $message")
    }

    fun open(code: String) {
        val item = store.find(code) ?: error("default attempt claim $code not found")
        validator.requireValid(item)
        item.moveTo(DefaultAttemptClaimStatus.PENDING)
        store.save(item)
        note("$code is pending")
    }

    fun complete(code: String, reason: String) {
        val item = store.find(code) ?: error("default attempt claim $code not found")
        if (item.isTerminal()) {
            note("$code already closed")
            return
        }
        item.moveTo(DefaultAttemptClaimStatus.CLOSED)
        store.save(item)
        note("$code is closed: $reason")
    }

    fun evaluate(code: String): Double {
        val item = store.find(code) ?: error("default attempt claim $code not found")
        val result = engine.evaluate(item)
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

fun newDefaultAttemptClaimManager(store: DefaultAttemptClaimStore, clock: Clock, flags: Registry): DefaultAttemptClaimManager {
    return DefaultAttemptClaimManager(
        store = store,
        validator = defaultDefaultAttemptClaimValidator(),
        clock = clock,
        journal = mutableListOf<String>(),
        engine = defaultDefaultAttemptClaimEngine(flags),
    )
}
