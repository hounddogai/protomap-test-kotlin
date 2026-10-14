package com.acme.transport.crewing.availability.claim

import com.acme.transport.crewing.clock.Clock
import com.acme.transport.kit.flags.Registry

/** Moves availability policies through their life cycle and keeps a journal of what it did. */
data class AvailabilityPolicyCoordinator(
    var store: AvailabilityPolicyStore,
    var validator: AvailabilityPolicyValidator,
    var clock: Clock,
    var journal: MutableList<String>,
    var engine: AvailabilityPolicyEngine,
) {
    private fun note(message: String) {
        journal.add("${clock.now()} $message")
    }

    fun submit(code: String) {
        val item = store.find(code) ?: error("availability policy $code not found")
        validator.requireValid(item)
        item.moveTo(AvailabilityPolicyStatus.GRANTED)
        store.save(item)
        note("$code is granted")
    }

    fun finish(code: String, reason: String) {
        val item = store.find(code) ?: error("availability policy $code not found")
        if (item.isTerminal()) {
            note("$code already closed")
            return
        }
        item.moveTo(AvailabilityPolicyStatus.EXPIRED)
        store.save(item)
        note("$code is expired: $reason")
    }

    fun quote(code: String): Double {
        val item = store.find(code) ?: error("availability policy $code not found")
        val result = engine.evaluate(item)
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

fun newAvailabilityPolicyCoordinator(store: AvailabilityPolicyStore, clock: Clock, flags: Registry): AvailabilityPolicyCoordinator {
    return AvailabilityPolicyCoordinator(
        store = store,
        validator = defaultAvailabilityPolicyValidator(),
        clock = clock,
        journal = mutableListOf<String>(),
        engine = defaultAvailabilityPolicyEngine(flags),
    )
}
