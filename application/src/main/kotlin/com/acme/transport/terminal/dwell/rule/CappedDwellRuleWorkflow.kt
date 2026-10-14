package com.acme.transport.terminal.dwell.rule

import com.acme.transport.kit.flags.Registry
import com.acme.transport.terminal.clock.Clock

/** Moves capped dwell rules through their life cycle and keeps a journal of what it did. */
data class CappedDwellRuleManager(
    var store: CappedDwellRuleStore,
    var validator: CappedDwellRuleValidator,
    var clock: Clock,
    var journal: MutableList<String>,
    var engine: CappedDwellRuleEngine,
) {
    private fun note(message: String) {
        journal.add("${clock.now()} $message")
    }

    fun start(code: String) {
        val item = store.find(code) ?: error("capped dwell rule $code not found")
        validator.requireValid(item)
        item.moveTo(CappedDwellRuleStatus.IN_REVIEW)
        store.save(item)
        note("$code is in review")
    }

    fun settle(code: String, reason: String) {
        val item = store.find(code) ?: error("capped dwell rule $code not found")
        if (item.isTerminal()) {
            note("$code already closed")
            return
        }
        item.moveTo(CappedDwellRuleStatus.ARCHIVED)
        store.save(item)
        note("$code is archived: $reason")
    }

    fun estimate(code: String): Double {
        val item = store.find(code) ?: error("capped dwell rule $code not found")
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

fun newCappedDwellRuleManager(store: CappedDwellRuleStore, clock: Clock, flags: Registry): CappedDwellRuleManager {
    return CappedDwellRuleManager(
        store = store,
        validator = defaultCappedDwellRuleValidator(),
        clock = clock,
        journal = mutableListOf<String>(),
        engine = defaultCappedDwellRuleEngine(flags),
    )
}
