package com.acme.transport.linehaul.laneplan.rule

import com.acme.transport.kit.flags.Registry
import com.acme.transport.linehaul.clock.Clock

/** Moves weighted lane plan rules through their life cycle and keeps a journal of what it did. */
data class WeightedLanePlanRuleService(
    var store: WeightedLanePlanRuleStore,
    var validator: WeightedLanePlanRuleValidator,
    var clock: Clock,
    var journal: MutableList<String>,
    var engine: WeightedLanePlanRuleEngine,
) {
    private fun note(message: String) {
        journal.add("${clock.now()} $message")
    }

    fun start(code: String) {
        val item = store.find(code) ?: error("weighted lane plan rule $code not found")
        validator.requireValid(item)
        item.moveTo(WeightedLanePlanRuleStatus.VALIDATED)
        store.save(item)
        note("$code is validated")
    }

    fun retire(code: String, reason: String) {
        val item = store.find(code) ?: error("weighted lane plan rule $code not found")
        if (item.isTerminal()) {
            note("$code already closed")
            return
        }
        item.moveTo(WeightedLanePlanRuleStatus.RETIRED)
        store.save(item)
        note("$code is retired: $reason")
    }

    fun evaluate(code: String): Double {
        val item = store.find(code) ?: error("weighted lane plan rule $code not found")
        val result = engine.evaluate(item)
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

fun newWeightedLanePlanRuleService(store: WeightedLanePlanRuleStore, clock: Clock, flags: Registry): WeightedLanePlanRuleService {
    return WeightedLanePlanRuleService(
        store = store,
        validator = defaultWeightedLanePlanRuleValidator(),
        clock = clock,
        journal = mutableListOf<String>(),
        engine = defaultWeightedLanePlanRuleEngine(flags),
    )
}
