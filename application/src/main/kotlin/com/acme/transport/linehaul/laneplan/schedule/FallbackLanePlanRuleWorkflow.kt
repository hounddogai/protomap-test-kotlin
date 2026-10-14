package com.acme.transport.linehaul.laneplan.schedule

import com.acme.transport.linehaul.clock.Clock

/** Moves fallback lane plan rules through their life cycle and keeps a journal of what it did. */
data class FallbackLanePlanRuleManager(
    var store: FallbackLanePlanRuleStore,
    var validator: FallbackLanePlanRuleValidator,
    var clock: Clock,
    var journal: MutableList<String>,
    var engine: FallbackLanePlanRuleEngine,
) {
    private fun note(message: String) {
        journal.add("${clock.now()} $message")
    }

    fun open(code: String) {
        val item = store.find(code) ?: error("fallback lane plan rule $code not found")
        validator.requireValid(item)
        item.moveTo(FallbackLanePlanRuleStatus.PENDING)
        store.save(item)
        note("$code is pending")
    }

    fun finish(code: String, reason: String) {
        val item = store.find(code) ?: error("fallback lane plan rule $code not found")
        if (item.isTerminal()) {
            note("$code already closed")
            return
        }
        item.moveTo(FallbackLanePlanRuleStatus.CLOSED)
        store.save(item)
        note("$code is closed: $reason")
    }

    fun score(code: String): Double {
        val item = store.find(code) ?: error("fallback lane plan rule $code not found")
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
            if (item.createdAt > cutoff) {
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

fun newFallbackLanePlanRuleManager(store: FallbackLanePlanRuleStore, clock: Clock): FallbackLanePlanRuleManager {
    return FallbackLanePlanRuleManager(
        store = store,
        validator = defaultFallbackLanePlanRuleValidator(),
        clock = clock,
        journal = mutableListOf<String>(),
        engine = defaultFallbackLanePlanRuleEngine(),
    )
}
