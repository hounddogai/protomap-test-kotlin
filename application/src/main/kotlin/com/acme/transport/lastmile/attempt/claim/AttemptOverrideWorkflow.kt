package com.acme.transport.lastmile.attempt.claim

import com.acme.transport.kit.flags.Registry
import com.acme.transport.lastmile.clock.Clock

/** Moves attempt overrides through their life cycle and keeps a journal of what it did. */
data class AttemptOverrideService(
    var store: AttemptOverrideStore,
    var validator: AttemptOverrideValidator,
    var clock: Clock,
    var journal: MutableList<String>,
    var engine: AttemptOverrideEngine,
) {
    private fun note(message: String) {
        journal.add("${clock.now()} $message")
    }

    fun submit(code: String) {
        val item = store.find(code) ?: error("attempt override $code not found")
        validator.requireValid(item)
        item.moveTo(AttemptOverrideStatus.IN_REVIEW)
        store.save(item)
        note("$code is in review")
    }

    fun complete(code: String, reason: String) {
        val item = store.find(code) ?: error("attempt override $code not found")
        if (item.isTerminal()) {
            note("$code already closed")
            return
        }
        item.moveTo(AttemptOverrideStatus.ARCHIVED)
        store.save(item)
        note("$code is archived: $reason")
    }

    fun estimate(code: String): Double {
        val item = store.find(code) ?: error("attempt override $code not found")
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

fun newAttemptOverrideService(store: AttemptOverrideStore, clock: Clock, flags: Registry): AttemptOverrideService {
    return AttemptOverrideService(
        store = store,
        validator = defaultAttemptOverrideValidator(),
        clock = clock,
        journal = mutableListOf<String>(),
        engine = defaultAttemptOverrideEngine(flags),
    )
}
