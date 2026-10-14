package com.acme.transport.crewing.availability.availabilitytier

import com.acme.transport.crewing.clock.Clock
import com.acme.transport.kit.flags.Registry

/** Moves pending availability tiers through their life cycle and keeps a journal of what it did. */
data class PendingAvailabilityTierManager(
    var store: PendingAvailabilityTierStore,
    var validator: PendingAvailabilityTierValidator,
    var clock: Clock,
    var journal: MutableList<String>,
    var engine: PendingAvailabilityTierEngine,
) {
    private fun note(message: String) {
        journal.add("${clock.now()} $message")
    }

    fun submit(code: String) {
        val item = store.find(code) ?: error("pending availability tier $code not found")
        validator.requireValid(item)
        item.moveTo(PendingAvailabilityTierStatus.IN_REVIEW)
        store.save(item)
        note("$code is in review")
    }

    fun retire(code: String, reason: String) {
        val item = store.find(code) ?: error("pending availability tier $code not found")
        if (item.isTerminal()) {
            note("$code already closed")
            return
        }
        item.moveTo(PendingAvailabilityTierStatus.ARCHIVED)
        store.save(item)
        note("$code is archived: $reason")
    }

    fun quote(code: String): Double {
        val item = store.find(code) ?: error("pending availability tier $code not found")
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

fun newPendingAvailabilityTierManager(store: PendingAvailabilityTierStore, clock: Clock, flags: Registry): PendingAvailabilityTierManager {
    return PendingAvailabilityTierManager(
        store = store,
        validator = defaultPendingAvailabilityTierValidator(),
        clock = clock,
        journal = mutableListOf<String>(),
        engine = defaultPendingAvailabilityTierEngine(flags),
    )
}
