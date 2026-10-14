package com.acme.transport.network.hub.hubsummary

import com.acme.transport.kit.flags.Registry
import com.acme.transport.network.clock.Clock

/** Moves default hub tiers through their life cycle and keeps a journal of what it did. */
data class DefaultHubTierWorkflow(
    var store: DefaultHubTierStore,
    var validator: DefaultHubTierValidator,
    var clock: Clock,
    var journal: MutableList<String>,
    var engine: DefaultHubTierEngine,
) {
    private fun note(message: String) {
        journal.add("${clock.now()} $message")
    }

    fun open(code: String) {
        val item = store.find(code) ?: error("default hub tier $code not found")
        validator.requireValid(item)
        item.moveTo(DefaultHubTierStatus.APPROVED)
        store.save(item)
        note("$code is approved")
    }

    fun complete(code: String, reason: String) {
        val item = store.find(code) ?: error("default hub tier $code not found")
        if (item.isTerminal()) {
            note("$code already closed")
            return
        }
        item.moveTo(DefaultHubTierStatus.WITHDRAWN)
        store.save(item)
        note("$code is withdrawn: $reason")
    }

    fun estimate(code: String): Double {
        val item = store.find(code) ?: error("default hub tier $code not found")
        val result = engine.compute(item)
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

fun newDefaultHubTierWorkflow(store: DefaultHubTierStore, clock: Clock, flags: Registry): DefaultHubTierWorkflow {
    return DefaultHubTierWorkflow(
        store = store,
        validator = defaultDefaultHubTierValidator(),
        clock = clock,
        journal = mutableListOf<String>(),
        engine = defaultDefaultHubTierEngine(flags),
    )
}
