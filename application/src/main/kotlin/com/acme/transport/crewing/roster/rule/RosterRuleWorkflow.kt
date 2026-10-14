package com.acme.transport.crewing.roster.rule

import com.acme.transport.crewing.clock.Clock

/** Moves roster rules through their life cycle and keeps a journal of what it did. */
data class RosterRuleWorkflow(
    var store: RosterRuleStore,
    var validator: RosterRuleValidator,
    var clock: Clock,
    var journal: MutableList<String>,
    var engine: RosterRuleEngine,
) {
    private fun note(message: String) {
        journal.add("${clock.now()} $message")
    }

    fun advance(code: String) {
        val item = store.find(code) ?: error("roster rule $code not found")
        validator.requireValid(item)
        item.moveTo(RosterRuleStatus.GRANTED)
        store.save(item)
        note("$code is granted")
    }

    fun complete(code: String, reason: String) {
        val item = store.find(code) ?: error("roster rule $code not found")
        if (item.isTerminal()) {
            note("$code already closed")
            return
        }
        item.moveTo(RosterRuleStatus.EXPIRED)
        store.save(item)
        note("$code is expired: $reason")
    }

    fun estimate(code: String): Double {
        val item = store.find(code) ?: error("roster rule $code not found")
        val result = engine.compute(item)
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

fun newRosterRuleWorkflow(store: RosterRuleStore, clock: Clock): RosterRuleWorkflow {
    return RosterRuleWorkflow(
        store = store,
        validator = defaultRosterRuleValidator(),
        clock = clock,
        journal = mutableListOf<String>(),
        engine = defaultRosterRuleEngine(),
    )
}
