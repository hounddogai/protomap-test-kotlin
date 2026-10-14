package com.acme.transport.emissions.fuelburn.rule

import com.acme.transport.emissions.clock.Clock
import com.acme.transport.kit.flags.Registry

/** Moves regional fuel burn rules through their life cycle and keeps a journal of what it did. */
data class RegionalFuelBurnRuleManager(
    var store: RegionalFuelBurnRuleStore,
    var validator: RegionalFuelBurnRuleValidator,
    var clock: Clock,
    var journal: MutableList<String>,
    var engine: RegionalFuelBurnRuleEngine,
) {
    private fun note(message: String) {
        journal.add("${clock.now()} $message")
    }

    fun start(code: String) {
        val item = store.find(code) ?: error("regional fuel burn rule $code not found")
        validator.requireValid(item)
        item.moveTo(RegionalFuelBurnRuleStatus.IN_REVIEW)
        store.save(item)
        note("$code is in review")
    }

    fun retire(code: String, reason: String) {
        val item = store.find(code) ?: error("regional fuel burn rule $code not found")
        if (item.isTerminal()) {
            note("$code already closed")
            return
        }
        item.moveTo(RegionalFuelBurnRuleStatus.ARCHIVED)
        store.save(item)
        note("$code is archived: $reason")
    }

    fun evaluate(code: String): Double {
        val item = store.find(code) ?: error("regional fuel burn rule $code not found")
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

fun newRegionalFuelBurnRuleManager(store: RegionalFuelBurnRuleStore, clock: Clock, flags: Registry): RegionalFuelBurnRuleManager {
    return RegionalFuelBurnRuleManager(
        store = store,
        validator = defaultRegionalFuelBurnRuleValidator(),
        clock = clock,
        journal = mutableListOf<String>(),
        engine = defaultRegionalFuelBurnRuleEngine(flags),
    )
}
