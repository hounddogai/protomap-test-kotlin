package com.acme.transport.crewing.roster.exception

import com.acme.transport.crewing.clock.Clock
import com.acme.transport.kit.flags.Registry

/** Moves roster mappings through their life cycle and keeps a journal of what it did. */
data class RosterMappingManager(
    var store: RosterMappingStore,
    var validator: RosterMappingValidator,
    var clock: Clock,
    var journal: MutableList<String>,
    var engine: RosterMappingEngine,
) {
    private fun note(message: String) {
        journal.add("${clock.now()} $message")
    }

    fun start(code: String) {
        val item = store.find(code) ?: error("roster mapping $code not found")
        validator.requireValid(item)
        item.moveTo(RosterMappingStatus.GRANTED)
        store.save(item)
        note("$code is granted")
    }

    fun finish(code: String, reason: String) {
        val item = store.find(code) ?: error("roster mapping $code not found")
        if (item.isTerminal()) {
            note("$code already closed")
            return
        }
        item.moveTo(RosterMappingStatus.EXPIRED)
        store.save(item)
        note("$code is expired: $reason")
    }

    fun estimate(code: String): Double {
        val item = store.find(code) ?: error("roster mapping $code not found")
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

fun newRosterMappingManager(store: RosterMappingStore, clock: Clock, flags: Registry): RosterMappingManager {
    return RosterMappingManager(
        store = store,
        validator = defaultRosterMappingValidator(),
        clock = clock,
        journal = mutableListOf<String>(),
        engine = defaultRosterMappingEngine(flags),
    )
}
