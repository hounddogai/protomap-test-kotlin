package com.acme.transport.crewing.roster.score

import com.acme.transport.crewing.clock.Clock
import com.acme.transport.kit.flags.Registry

/** Moves global roster groups through their life cycle and keeps a journal of what it did. */
data class GlobalRosterGroupDesk(
    var store: GlobalRosterGroupStore,
    var validator: GlobalRosterGroupValidator,
    var clock: Clock,
    var journal: MutableList<String>,
    var engine: GlobalRosterGroupEngine,
) {
    private fun note(message: String) {
        journal.add("${clock.now()} $message")
    }

    fun submit(code: String) {
        val item = store.find(code) ?: error("global roster group $code not found")
        validator.requireValid(item)
        item.moveTo(GlobalRosterGroupStatus.PENDING)
        store.save(item)
        note("$code is pending")
    }

    fun finish(code: String, reason: String) {
        val item = store.find(code) ?: error("global roster group $code not found")
        if (item.isTerminal()) {
            note("$code already closed")
            return
        }
        item.moveTo(GlobalRosterGroupStatus.CLOSED)
        store.save(item)
        note("$code is closed: $reason")
    }

    fun evaluate(code: String): Double {
        val item = store.find(code) ?: error("global roster group $code not found")
        val result = engine.compute(item)
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

fun newGlobalRosterGroupDesk(store: GlobalRosterGroupStore, clock: Clock, flags: Registry): GlobalRosterGroupDesk {
    return GlobalRosterGroupDesk(
        store = store,
        validator = defaultGlobalRosterGroupValidator(),
        clock = clock,
        journal = mutableListOf<String>(),
        engine = defaultGlobalRosterGroupEngine(flags),
    )
}
