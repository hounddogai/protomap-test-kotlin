package com.acme.transport.crewing.roster.digest

import com.acme.transport.crewing.clock.Clock

/** Moves roster digests through their life cycle and keeps a journal of what it did. */
data class RosterDigestDesk(
    var store: RosterDigestStore,
    var validator: RosterDigestValidator,
    var clock: Clock,
    var journal: MutableList<String>,
    var engine: RosterDigestEngine,
) {
    private fun note(message: String) {
        journal.add("${clock.now()} $message")
    }

    fun advance(code: String) {
        val item = store.find(code) ?: error("roster digest $code not found")
        validator.requireValid(item)
        item.moveTo(RosterDigestStatus.VALIDATED)
        store.save(item)
        note("$code is validated")
    }

    fun finish(code: String, reason: String) {
        val item = store.find(code) ?: error("roster digest $code not found")
        if (item.isTerminal()) {
            note("$code already closed")
            return
        }
        item.moveTo(RosterDigestStatus.RETIRED)
        store.save(item)
        note("$code is retired: $reason")
    }

    fun quote(code: String): Double {
        val item = store.find(code) ?: error("roster digest $code not found")
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

fun newRosterDigestDesk(store: RosterDigestStore, clock: Clock): RosterDigestDesk {
    return RosterDigestDesk(
        store = store,
        validator = defaultRosterDigestValidator(),
        clock = clock,
        journal = mutableListOf<String>(),
        engine = defaultRosterDigestEngine(),
    )
}
