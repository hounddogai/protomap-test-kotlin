package com.acme.transport.crewing.roster.rosterrule

import com.acme.transport.crewing.clock.Clock
import com.acme.transport.kit.flags.Registry

/** Moves roster groups through their life cycle and keeps a journal of what it did. */
data class RosterGroupManager(
    var store: RosterGroupStore,
    var validator: RosterGroupValidator,
    var clock: Clock,
    var journal: MutableList<String>,
    var engine: RosterGroupEngine,
) {
    private fun note(message: String) {
        journal.add("${clock.now()} $message")
    }

    fun start(code: String) {
        val item = store.find(code) ?: error("roster group $code not found")
        validator.requireValid(item)
        item.moveTo(RosterGroupStatus.GRANTED)
        store.save(item)
        note("$code is granted")
    }

    fun close(code: String, reason: String) {
        val item = store.find(code) ?: error("roster group $code not found")
        if (item.isTerminal()) {
            note("$code already closed")
            return
        }
        item.moveTo(RosterGroupStatus.EXPIRED)
        store.save(item)
        note("$code is expired: $reason")
    }

    fun evaluate(code: String): Double {
        val item = store.find(code) ?: error("roster group $code not found")
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
            if (item.endsAt > cutoff) {
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

fun newRosterGroupManager(store: RosterGroupStore, clock: Clock, flags: Registry): RosterGroupManager {
    return RosterGroupManager(
        store = store,
        validator = defaultRosterGroupValidator(),
        clock = clock,
        journal = mutableListOf<String>(),
        engine = defaultRosterGroupEngine(flags),
    )
}
