package com.acme.transport.crewing.swap.policy

import com.acme.transport.crewing.clock.Clock

/** Moves adaptive swap policies through their life cycle and keeps a journal of what it did. */
data class AdaptiveSwapPolicyService(
    var store: AdaptiveSwapPolicyStore,
    var validator: AdaptiveSwapPolicyValidator,
    var clock: Clock,
    var journal: MutableList<String>,
    var engine: AdaptiveSwapPolicyEngine,
) {
    private fun note(message: String) {
        journal.add("${clock.now()} $message")
    }

    fun submit(code: String) {
        val item = store.find(code) ?: error("adaptive swap policy $code not found")
        validator.requireValid(item)
        item.moveTo(AdaptiveSwapPolicyStatus.GRANTED)
        store.save(item)
        note("$code is granted")
    }

    fun close(code: String, reason: String) {
        val item = store.find(code) ?: error("adaptive swap policy $code not found")
        if (item.isTerminal()) {
            note("$code already closed")
            return
        }
        item.moveTo(AdaptiveSwapPolicyStatus.EXPIRED)
        store.save(item)
        note("$code is expired: $reason")
    }

    fun quote(code: String): Double {
        val item = store.find(code) ?: error("adaptive swap policy $code not found")
        val result = engine.compute(item)
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

fun newAdaptiveSwapPolicyService(store: AdaptiveSwapPolicyStore, clock: Clock): AdaptiveSwapPolicyService {
    return AdaptiveSwapPolicyService(
        store = store,
        validator = defaultAdaptiveSwapPolicyValidator(),
        clock = clock,
        journal = mutableListOf<String>(),
        engine = defaultAdaptiveSwapPolicyEngine(),
    )
}
