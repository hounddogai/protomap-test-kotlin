package com.acme.transport.customs.hold.fallbackholdaudit

import com.acme.transport.customs.clock.Clock
import com.acme.transport.kit.flags.Registry

/** Moves fallback hold policies through their life cycle and keeps a journal of what it did. */
data class FallbackHoldPolicyCoordinator(
    var store: FallbackHoldPolicyStore,
    var validator: FallbackHoldPolicyValidator,
    var clock: Clock,
    var journal: MutableList<String>,
    var engine: FallbackHoldPolicyEngine,
) {
    private fun note(message: String) {
        journal.add("${clock.now()} $message")
    }

    fun open(code: String) {
        val item = store.find(code) ?: error("fallback hold policy $code not found")
        validator.requireValid(item)
        item.moveTo(FallbackHoldPolicyStatus.VALIDATED)
        store.save(item)
        note("$code is validated")
    }

    fun retire(code: String, reason: String) {
        val item = store.find(code) ?: error("fallback hold policy $code not found")
        if (item.isTerminal()) {
            note("$code already closed")
            return
        }
        item.moveTo(FallbackHoldPolicyStatus.RETIRED)
        store.save(item)
        note("$code is retired: $reason")
    }

    fun score(code: String): Double {
        val item = store.find(code) ?: error("fallback hold policy $code not found")
        val result = engine.evaluate(item)
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

fun newFallbackHoldPolicyCoordinator(store: FallbackHoldPolicyStore, clock: Clock, flags: Registry): FallbackHoldPolicyCoordinator {
    return FallbackHoldPolicyCoordinator(
        store = store,
        validator = defaultFallbackHoldPolicyValidator(),
        clock = clock,
        journal = mutableListOf<String>(),
        engine = defaultFallbackHoldPolicyEngine(flags),
    )
}
