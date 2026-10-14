package com.acme.transport.crewing.availability.tier

import com.acme.transport.crewing.clock.Clock
import com.acme.transport.kit.flags.Registry

/** Moves availability tiers through their life cycle and keeps a journal of what it did. */
data class AvailabilityTierWorkflow(
    var store: AvailabilityTierStore,
    var validator: AvailabilityTierValidator,
    var clock: Clock,
    var journal: MutableList<String>,
    var engine: AvailabilityTierEngine,
) {
    private fun note(message: String) {
        journal.add("${clock.now()} $message")
    }

    fun submit(code: String) {
        val item = store.find(code) ?: error("availability tier $code not found")
        validator.requireValid(item)
        item.moveTo(AvailabilityTierStatus.IN_REVIEW)
        store.save(item)
        note("$code is in review")
    }

    fun retire(code: String, reason: String) {
        val item = store.find(code) ?: error("availability tier $code not found")
        if (item.isTerminal()) {
            note("$code already closed")
            return
        }
        item.moveTo(AvailabilityTierStatus.ARCHIVED)
        store.save(item)
        note("$code is archived: $reason")
    }

    fun quote(code: String): Double {
        val item = store.find(code) ?: error("availability tier $code not found")
        val result = engine.price(item)
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

fun newAvailabilityTierWorkflow(store: AvailabilityTierStore, clock: Clock, flags: Registry): AvailabilityTierWorkflow {
    return AvailabilityTierWorkflow(
        store = store,
        validator = defaultAvailabilityTierValidator(),
        clock = clock,
        journal = mutableListOf<String>(),
        engine = defaultAvailabilityTierEngine(flags),
    )
}
