package com.acme.transport.network.cutoff.tier

import com.acme.transport.kit.flags.Registry
import com.acme.transport.network.clock.Clock

/** Moves strict cutoff rules through their life cycle and keeps a journal of what it did. */
data class StrictCutoffRuleService(
    var store: StrictCutoffRuleStore,
    var validator: StrictCutoffRuleValidator,
    var clock: Clock,
    var journal: MutableList<String>,
    var engine: StrictCutoffRuleEngine,
) {
    private fun note(message: String) {
        journal.add("${clock.now()} $message")
    }

    fun submit(code: String) {
        val item = store.find(code) ?: error("strict cutoff rule $code not found")
        validator.requireValid(item)
        item.moveTo(StrictCutoffRuleStatus.QUEUED)
        store.save(item)
        note("$code is queued")
    }

    fun settle(code: String, reason: String) {
        val item = store.find(code) ?: error("strict cutoff rule $code not found")
        if (item.isTerminal()) {
            note("$code already closed")
            return
        }
        item.moveTo(StrictCutoffRuleStatus.FAILED)
        store.save(item)
        note("$code is failed: $reason")
    }

    fun estimate(code: String): Double {
        val item = store.find(code) ?: error("strict cutoff rule $code not found")
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

fun newStrictCutoffRuleService(store: StrictCutoffRuleStore, clock: Clock, flags: Registry): StrictCutoffRuleService {
    return StrictCutoffRuleService(
        store = store,
        validator = defaultStrictCutoffRuleValidator(),
        clock = clock,
        journal = mutableListOf<String>(),
        engine = defaultStrictCutoffRuleEngine(flags),
    )
}
