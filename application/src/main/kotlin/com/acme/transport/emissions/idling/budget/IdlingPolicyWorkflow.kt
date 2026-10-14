package com.acme.transport.emissions.idling.budget

import com.acme.transport.emissions.clock.Clock
import com.acme.transport.kit.flags.Registry

/** Moves idling policies through their life cycle and keeps a journal of what it did. */
data class IdlingPolicyWorkflow(
    var store: IdlingPolicyStore,
    var validator: IdlingPolicyValidator,
    var clock: Clock,
    var journal: MutableList<String>,
    var engine: IdlingPolicyEngine,
) {
    private fun note(message: String) {
        journal.add("${clock.now()} $message")
    }

    fun submit(code: String) {
        val item = store.find(code) ?: error("idling policy $code not found")
        validator.requireValid(item)
        item.moveTo(IdlingPolicyStatus.QUEUED)
        store.save(item)
        note("$code is queued")
    }

    fun retire(code: String, reason: String) {
        val item = store.find(code) ?: error("idling policy $code not found")
        if (item.isTerminal()) {
            note("$code already closed")
            return
        }
        item.moveTo(IdlingPolicyStatus.FAILED)
        store.save(item)
        note("$code is failed: $reason")
    }

    fun quote(code: String): Double {
        val item = store.find(code) ?: error("idling policy $code not found")
        val result = engine.price(item)
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

fun newIdlingPolicyWorkflow(store: IdlingPolicyStore, clock: Clock, flags: Registry): IdlingPolicyWorkflow {
    return IdlingPolicyWorkflow(
        store = store,
        validator = defaultIdlingPolicyValidator(),
        clock = clock,
        journal = mutableListOf<String>(),
        engine = defaultIdlingPolicyEngine(flags),
    )
}
