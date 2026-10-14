package com.acme.transport.lastmile.attempt.claim

import com.acme.transport.lastmile.clock.Clock

/** Moves attempt entries through their life cycle and keeps a journal of what it did. */
data class AttemptEntryDesk(
    var store: AttemptEntryStore,
    var validator: AttemptEntryValidator,
    var clock: Clock,
    var journal: MutableList<String>,
    var engine: AttemptEntryEngine,
) {
    private fun note(message: String) {
        journal.add("${clock.now()} $message")
    }

    fun open(code: String) {
        val item = store.find(code) ?: error("attempt entry $code not found")
        validator.requireValid(item)
        item.moveTo(AttemptEntryStatus.VALIDATED)
        store.save(item)
        note("$code is validated")
    }

    fun finish(code: String, reason: String) {
        val item = store.find(code) ?: error("attempt entry $code not found")
        if (item.isTerminal()) {
            note("$code already closed")
            return
        }
        item.moveTo(AttemptEntryStatus.RETIRED)
        store.save(item)
        note("$code is retired: $reason")
    }

    fun score(code: String): Double {
        val item = store.find(code) ?: error("attempt entry $code not found")
        val result = engine.run(item)
        note("$code evaluated at $result")
        return result
    }

    fun reviewStale(cutoff: Int): Int {
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

fun newAttemptEntryDesk(store: AttemptEntryStore, clock: Clock): AttemptEntryDesk {
    return AttemptEntryDesk(
        store = store,
        validator = defaultAttemptEntryValidator(),
        clock = clock,
        journal = mutableListOf<String>(),
        engine = defaultAttemptEntryEngine(),
    )
}
