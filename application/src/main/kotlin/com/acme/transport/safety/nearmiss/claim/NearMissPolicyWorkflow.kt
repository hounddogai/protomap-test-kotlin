package com.acme.transport.safety.nearmiss.claim

import com.acme.transport.kit.flags.Registry
import com.acme.transport.safety.clock.Clock

/** Moves near miss policies through their life cycle and keeps a journal of what it did. */
data class NearMissPolicyManager(
    var store: NearMissPolicyStore,
    var validator: NearMissPolicyValidator,
    var clock: Clock,
    var journal: MutableList<String>,
    var engine: NearMissPolicyEngine,
) {
    private fun note(message: String) {
        journal.add("${clock.now()} $message")
    }

    fun open(code: String) {
        val item = store.find(code) ?: error("near miss policy $code not found")
        validator.requireValid(item)
        item.moveTo(NearMissPolicyStatus.VALIDATED)
        store.save(item)
        note("$code is validated")
    }

    fun retire(code: String, reason: String) {
        val item = store.find(code) ?: error("near miss policy $code not found")
        if (item.isTerminal()) {
            note("$code already closed")
            return
        }
        item.moveTo(NearMissPolicyStatus.RETIRED)
        store.save(item)
        note("$code is retired: $reason")
    }

    fun estimate(code: String): Double {
        val item = store.find(code) ?: error("near miss policy $code not found")
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

fun newNearMissPolicyManager(store: NearMissPolicyStore, clock: Clock, flags: Registry): NearMissPolicyManager {
    return NearMissPolicyManager(
        store = store,
        validator = defaultNearMissPolicyValidator(),
        clock = clock,
        journal = mutableListOf<String>(),
        engine = defaultNearMissPolicyEngine(flags),
    )
}
