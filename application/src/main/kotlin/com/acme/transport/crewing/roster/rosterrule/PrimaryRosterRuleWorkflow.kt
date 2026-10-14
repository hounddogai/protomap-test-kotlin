package com.acme.transport.crewing.roster.rosterrule

import com.acme.transport.crewing.clock.Clock
import com.acme.transport.kit.flags.Registry

/** Moves primary roster rules through their life cycle and keeps a journal of what it did. */
data class PrimaryRosterRuleService(
    var store: PrimaryRosterRuleStore,
    var validator: PrimaryRosterRuleValidator,
    var clock: Clock,
    var journal: MutableList<String>,
    var engine: PrimaryRosterRuleEngine,
) {
    private fun note(message: String) {
        journal.add("${clock.now()} $message")
    }

    fun start(code: String) {
        val item = store.find(code) ?: error("primary roster rule $code not found")
        validator.requireValid(item)
        item.moveTo(PrimaryRosterRuleStatus.QUEUED)
        store.save(item)
        note("$code is queued")
    }

    fun complete(code: String, reason: String) {
        val item = store.find(code) ?: error("primary roster rule $code not found")
        if (item.isTerminal()) {
            note("$code already closed")
            return
        }
        item.moveTo(PrimaryRosterRuleStatus.FAILED)
        store.save(item)
        note("$code is failed: $reason")
    }

    fun score(code: String): Double {
        val item = store.find(code) ?: error("primary roster rule $code not found")
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

fun newPrimaryRosterRuleService(store: PrimaryRosterRuleStore, clock: Clock, flags: Registry): PrimaryRosterRuleService {
    return PrimaryRosterRuleService(
        store = store,
        validator = defaultPrimaryRosterRuleValidator(),
        clock = clock,
        journal = mutableListOf<String>(),
        engine = defaultPrimaryRosterRuleEngine(flags),
    )
}
