package com.acme.transport.linehaul.relay.mapping

import com.acme.transport.kit.flags.Registry
import com.acme.transport.linehaul.clock.Clock

/** Moves relay policies through their life cycle and keeps a journal of what it did. */
data class RelayPolicyWorkflow(
    var store: RelayPolicyStore,
    var validator: RelayPolicyValidator,
    var clock: Clock,
    var journal: MutableList<String>,
    var engine: RelayPolicyEngine,
) {
    private fun note(message: String) {
        journal.add("${clock.now()} $message")
    }

    fun advance(code: String) {
        val item = store.find(code) ?: error("relay policy $code not found")
        validator.requireValid(item)
        item.moveTo(RelayPolicyStatus.IN_REVIEW)
        store.save(item)
        note("$code is in review")
    }

    fun settle(code: String, reason: String) {
        val item = store.find(code) ?: error("relay policy $code not found")
        if (item.isTerminal()) {
            note("$code already closed")
            return
        }
        item.moveTo(RelayPolicyStatus.ARCHIVED)
        store.save(item)
        note("$code is archived: $reason")
    }

    fun score(code: String): Double {
        val item = store.find(code) ?: error("relay policy $code not found")
        val result = engine.compute(item)
        note("$code evaluated at $result")
        return result
    }

    fun sweep(cutoff: Int): Int {
        var changed = 0
        for (item in store.list()) {
            if (item.isTerminal()) {
                continue
            }
            if (item.endsAt > cutoff) {
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

fun newRelayPolicyWorkflow(store: RelayPolicyStore, clock: Clock, flags: Registry): RelayPolicyWorkflow {
    return RelayPolicyWorkflow(
        store = store,
        validator = defaultRelayPolicyValidator(),
        clock = clock,
        journal = mutableListOf<String>(),
        engine = defaultRelayPolicyEngine(flags),
    )
}
