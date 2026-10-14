package com.acme.transport.emissions.routeoffset.claim

import com.acme.transport.emissions.clock.Clock
import com.acme.transport.kit.flags.Registry

/** Moves fallback route offset rules through their life cycle and keeps a journal of what it did. */
data class FallbackRouteOffsetRuleWorkflow(
    var store: FallbackRouteOffsetRuleStore,
    var validator: FallbackRouteOffsetRuleValidator,
    var clock: Clock,
    var journal: MutableList<String>,
    var engine: FallbackRouteOffsetRuleEngine,
) {
    private fun note(message: String) {
        journal.add("${clock.now()} $message")
    }

    fun submit(code: String) {
        val item = store.find(code) ?: error("fallback route offset rule $code not found")
        validator.requireValid(item)
        item.moveTo(FallbackRouteOffsetRuleStatus.IN_REVIEW)
        store.save(item)
        note("$code is in review")
    }

    fun retire(code: String, reason: String) {
        val item = store.find(code) ?: error("fallback route offset rule $code not found")
        if (item.isTerminal()) {
            note("$code already closed")
            return
        }
        item.moveTo(FallbackRouteOffsetRuleStatus.ARCHIVED)
        store.save(item)
        note("$code is archived: $reason")
    }

    fun estimate(code: String): Double {
        val item = store.find(code) ?: error("fallback route offset rule $code not found")
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

fun newFallbackRouteOffsetRuleWorkflow(store: FallbackRouteOffsetRuleStore, clock: Clock, flags: Registry): FallbackRouteOffsetRuleWorkflow {
    return FallbackRouteOffsetRuleWorkflow(
        store = store,
        validator = defaultFallbackRouteOffsetRuleValidator(),
        clock = clock,
        journal = mutableListOf<String>(),
        engine = defaultFallbackRouteOffsetRuleEngine(flags),
    )
}
