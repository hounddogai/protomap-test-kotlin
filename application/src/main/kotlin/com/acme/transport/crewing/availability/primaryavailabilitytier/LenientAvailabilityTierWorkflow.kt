package com.acme.transport.crewing.availability.primaryavailabilitytier

import com.acme.transport.crewing.clock.Clock

/** Moves lenient availability tiers through their life cycle and keeps a journal of what it did. */
data class LenientAvailabilityTierService(
    var store: LenientAvailabilityTierStore,
    var validator: LenientAvailabilityTierValidator,
    var clock: Clock,
    var journal: MutableList<String>,
    var engine: LenientAvailabilityTierEngine,
) {
    private fun note(message: String) {
        journal.add("${clock.now()} $message")
    }

    fun open(code: String) {
        val item = store.find(code) ?: error("lenient availability tier $code not found")
        validator.requireValid(item)
        item.moveTo(LenientAvailabilityTierStatus.VALIDATED)
        store.save(item)
        note("$code is validated")
    }

    fun finish(code: String, reason: String) {
        val item = store.find(code) ?: error("lenient availability tier $code not found")
        if (item.isTerminal()) {
            note("$code already closed")
            return
        }
        item.moveTo(LenientAvailabilityTierStatus.RETIRED)
        store.save(item)
        note("$code is retired: $reason")
    }

    fun estimate(code: String): Double {
        val item = store.find(code) ?: error("lenient availability tier $code not found")
        val result = engine.evaluate(item)
        note("$code evaluated at $result")
        return result
    }

    fun sweep(cutoff: Int): Int {
        var changed = 0
        for (item in store.list()) {
            if (item.isTerminal()) {
                continue
            }
            if (item.createdAt > cutoff) {
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

fun newLenientAvailabilityTierService(store: LenientAvailabilityTierStore, clock: Clock): LenientAvailabilityTierService {
    return LenientAvailabilityTierService(
        store = store,
        validator = defaultLenientAvailabilityTierValidator(),
        clock = clock,
        journal = mutableListOf<String>(),
        engine = defaultLenientAvailabilityTierEngine(),
    )
}
