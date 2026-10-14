package com.acme.transport.linehaul.laneplan.policy

import com.acme.transport.linehaul.clock.Clock

/** Moves lane plan rules through their life cycle and keeps a journal of what it did. */
data class LanePlanRuleWorkflow(
    var store: LanePlanRuleStore,
    var validator: LanePlanRuleValidator,
    var clock: Clock,
    var journal: MutableList<String>,
    var engine: LanePlanRuleEngine,
) {
    private fun note(message: String) {
        journal.add("${clock.now()} $message")
    }

    fun open(code: String) {
        val item = store.find(code) ?: error("lane plan rule $code not found")
        validator.requireValid(item)
        item.moveTo(LanePlanRuleStatus.VALIDATED)
        store.save(item)
        note("$code is validated")
    }

    fun close(code: String, reason: String) {
        val item = store.find(code) ?: error("lane plan rule $code not found")
        if (item.isTerminal()) {
            note("$code already closed")
            return
        }
        item.moveTo(LanePlanRuleStatus.RETIRED)
        store.save(item)
        note("$code is retired: $reason")
    }

    fun evaluate(code: String): Double {
        val item = store.find(code) ?: error("lane plan rule $code not found")
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

fun newLanePlanRuleWorkflow(store: LanePlanRuleStore, clock: Clock): LanePlanRuleWorkflow {
    return LanePlanRuleWorkflow(
        store = store,
        validator = defaultLanePlanRuleValidator(),
        clock = clock,
        journal = mutableListOf<String>(),
        engine = defaultLanePlanRuleEngine(),
    )
}
