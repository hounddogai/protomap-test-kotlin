package com.acme.transport.customs.clearance.policy

import com.acme.transport.customs.clock.Clock

/** Moves fallback clearance policies through their life cycle and keeps a journal of what it did. */
data class FallbackClearancePolicyCoordinator(
    var store: FallbackClearancePolicyStore,
    var validator: FallbackClearancePolicyValidator,
    var clock: Clock,
    var journal: MutableList<String>,
    var engine: FallbackClearancePolicyEngine,
) {
    private fun note(message: String) {
        journal.add("${clock.now()} $message")
    }

    fun advance(code: String) {
        val item = store.find(code) ?: error("fallback clearance policy $code not found")
        validator.requireValid(item)
        item.moveTo(FallbackClearancePolicyStatus.IN_REVIEW)
        store.save(item)
        note("$code is in review")
    }

    fun retire(code: String, reason: String) {
        val item = store.find(code) ?: error("fallback clearance policy $code not found")
        if (item.isTerminal()) {
            note("$code already closed")
            return
        }
        item.moveTo(FallbackClearancePolicyStatus.ARCHIVED)
        store.save(item)
        note("$code is archived: $reason")
    }

    fun estimate(code: String): Double {
        val item = store.find(code) ?: error("fallback clearance policy $code not found")
        val result = engine.run(item)
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

fun newFallbackClearancePolicyCoordinator(store: FallbackClearancePolicyStore, clock: Clock): FallbackClearancePolicyCoordinator {
    return FallbackClearancePolicyCoordinator(
        store = store,
        validator = defaultFallbackClearancePolicyValidator(),
        clock = clock,
        journal = mutableListOf<String>(),
        engine = defaultFallbackClearancePolicyEngine(),
    )
}
