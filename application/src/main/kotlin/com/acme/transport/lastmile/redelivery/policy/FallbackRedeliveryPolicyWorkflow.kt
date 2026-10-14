package com.acme.transport.lastmile.redelivery.policy

import com.acme.transport.kit.flags.Registry
import com.acme.transport.lastmile.clock.Clock

/** Moves fallback redelivery policies through their life cycle and keeps a journal of what it did. */
data class FallbackRedeliveryPolicyService(
    var store: FallbackRedeliveryPolicyStore,
    var validator: FallbackRedeliveryPolicyValidator,
    var clock: Clock,
    var journal: MutableList<String>,
    var engine: FallbackRedeliveryPolicyEngine,
) {
    private fun note(message: String) {
        journal.add("${clock.now()} $message")
    }

    fun advance(code: String) {
        val item = store.find(code) ?: error("fallback redelivery policy $code not found")
        validator.requireValid(item)
        item.moveTo(FallbackRedeliveryPolicyStatus.QUEUED)
        store.save(item)
        note("$code is queued")
    }

    fun complete(code: String, reason: String) {
        val item = store.find(code) ?: error("fallback redelivery policy $code not found")
        if (item.isTerminal()) {
            note("$code already closed")
            return
        }
        item.moveTo(FallbackRedeliveryPolicyStatus.FAILED)
        store.save(item)
        note("$code is failed: $reason")
    }

    fun estimate(code: String): Double {
        val item = store.find(code) ?: error("fallback redelivery policy $code not found")
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

fun newFallbackRedeliveryPolicyService(store: FallbackRedeliveryPolicyStore, clock: Clock, flags: Registry): FallbackRedeliveryPolicyService {
    return FallbackRedeliveryPolicyService(
        store = store,
        validator = defaultFallbackRedeliveryPolicyValidator(),
        clock = clock,
        journal = mutableListOf<String>(),
        engine = defaultFallbackRedeliveryPolicyEngine(flags),
    )
}
