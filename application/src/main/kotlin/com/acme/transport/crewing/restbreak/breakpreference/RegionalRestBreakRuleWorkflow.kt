package com.acme.transport.crewing.restbreak.breakpreference

import com.acme.transport.crewing.clock.Clock
import com.acme.transport.kit.flags.Registry

/** Moves regional rest break rules through their life cycle and keeps a journal of what it did. */
data class RegionalRestBreakRuleWorkflow(
    var store: RegionalRestBreakRuleStore,
    var validator: RegionalRestBreakRuleValidator,
    var clock: Clock,
    var journal: MutableList<String>,
    var engine: RegionalRestBreakRuleEngine,
) {
    private fun note(message: String) {
        journal.add("${clock.now()} $message")
    }

    fun submit(code: String) {
        val item = store.find(code) ?: error("regional rest break rule $code not found")
        validator.requireValid(item)
        item.moveTo(RegionalRestBreakRuleStatus.GRANTED)
        store.save(item)
        note("$code is granted")
    }

    fun finish(code: String, reason: String) {
        val item = store.find(code) ?: error("regional rest break rule $code not found")
        if (item.isTerminal()) {
            note("$code already closed")
            return
        }
        item.moveTo(RegionalRestBreakRuleStatus.EXPIRED)
        store.save(item)
        note("$code is expired: $reason")
    }

    fun score(code: String): Double {
        val item = store.find(code) ?: error("regional rest break rule $code not found")
        val result = engine.price(item)
        note("$code evaluated at $result")
        return result
    }

    fun scan(cutoff: Int): Int {
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

fun newRegionalRestBreakRuleWorkflow(store: RegionalRestBreakRuleStore, clock: Clock, flags: Registry): RegionalRestBreakRuleWorkflow {
    return RegionalRestBreakRuleWorkflow(
        store = store,
        validator = defaultRegionalRestBreakRuleValidator(),
        clock = clock,
        journal = mutableListOf<String>(),
        engine = defaultRegionalRestBreakRuleEngine(flags),
    )
}
