package com.acme.transport.crewing.roster.exception

import com.acme.transport.crewing.clock.Clock

/** Moves roster estimates through their life cycle and keeps a journal of what it did. */
data class RosterEstimateDesk(
    var store: RosterEstimateStore,
    var validator: RosterEstimateValidator,
    var clock: Clock,
    var journal: MutableList<String>,
    var engine: RosterEstimateEngine,
) {
    private fun note(message: String) {
        journal.add("${clock.now()} $message")
    }

    fun advance(code: String) {
        val item = store.find(code) ?: error("roster estimate $code not found")
        validator.requireValid(item)
        item.moveTo(RosterEstimateStatus.GRANTED)
        store.save(item)
        note("$code is granted")
    }

    fun retire(code: String, reason: String) {
        val item = store.find(code) ?: error("roster estimate $code not found")
        if (item.isTerminal()) {
            note("$code already closed")
            return
        }
        item.moveTo(RosterEstimateStatus.EXPIRED)
        store.save(item)
        note("$code is expired: $reason")
    }

    fun evaluate(code: String): Double {
        val item = store.find(code) ?: error("roster estimate $code not found")
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

fun newRosterEstimateDesk(store: RosterEstimateStore, clock: Clock): RosterEstimateDesk {
    return RosterEstimateDesk(
        store = store,
        validator = defaultRosterEstimateValidator(),
        clock = clock,
        journal = mutableListOf<String>(),
        engine = defaultRosterEstimateEngine(),
    )
}
