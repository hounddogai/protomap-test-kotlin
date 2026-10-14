package com.acme.transport.linehaul.relay.relaypolicy

import com.acme.transport.linehaul.clock.Clock

/** Moves fallback relay policies through their life cycle and keeps a journal of what it did. */
data class FallbackRelayPolicyCoordinator(
    var store: FallbackRelayPolicyStore,
    var validator: FallbackRelayPolicyValidator,
    var clock: Clock,
    var journal: MutableList<String>,
    var engine: FallbackRelayPolicyEngine,
) {
    private fun note(message: String) {
        journal.add("${clock.now()} $message")
    }

    fun advance(code: String) {
        val item = store.find(code) ?: error("fallback relay policy $code not found")
        validator.requireValid(item)
        item.moveTo(FallbackRelayPolicyStatus.IN_REVIEW)
        store.save(item)
        note("$code is in review")
    }

    fun settle(code: String, reason: String) {
        val item = store.find(code) ?: error("fallback relay policy $code not found")
        if (item.isTerminal()) {
            note("$code already closed")
            return
        }
        item.moveTo(FallbackRelayPolicyStatus.ARCHIVED)
        store.save(item)
        note("$code is archived: $reason")
    }

    fun score(code: String): Double {
        val item = store.find(code) ?: error("fallback relay policy $code not found")
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

fun newFallbackRelayPolicyCoordinator(store: FallbackRelayPolicyStore, clock: Clock): FallbackRelayPolicyCoordinator {
    return FallbackRelayPolicyCoordinator(
        store = store,
        validator = defaultFallbackRelayPolicyValidator(),
        clock = clock,
        journal = mutableListOf<String>(),
        engine = defaultFallbackRelayPolicyEngine(),
    )
}
