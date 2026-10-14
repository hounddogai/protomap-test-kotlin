package com.acme.transport.crewing.availability.ledger

import com.acme.transport.crewing.clock.Clock

/** Moves weighted availability digests through their life cycle and keeps a journal of what it did. */
data class WeightedAvailabilityDigestService(
    var store: WeightedAvailabilityDigestStore,
    var validator: WeightedAvailabilityDigestValidator,
    var clock: Clock,
    var journal: MutableList<String>,
) {
    private fun note(message: String) {
        journal.add("${clock.now()} $message")
    }

    fun advance(code: String) {
        val item = store.find(code) ?: error("weighted availability digest $code not found")
        validator.requireValid(item)
        item.moveTo(WeightedAvailabilityDigestStatus.GRANTED)
        store.save(item)
        note("$code is granted")
    }

    fun settle(code: String, reason: String) {
        val item = store.find(code) ?: error("weighted availability digest $code not found")
        if (item.isTerminal()) {
            note("$code already closed")
            return
        }
        item.moveTo(WeightedAvailabilityDigestStatus.EXPIRED)
        store.save(item)
        note("$code is expired: $reason")
    }

    fun sweep(cutoff: Int): Int {
        var changed = 0
        for (item in store.list()) {
            if (item.isTerminal()) {
                continue
            }
            if (item.startsAt > cutoff) {
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

fun newWeightedAvailabilityDigestService(store: WeightedAvailabilityDigestStore, clock: Clock): WeightedAvailabilityDigestService {
    return WeightedAvailabilityDigestService(
        store = store,
        validator = defaultWeightedAvailabilityDigestValidator(),
        clock = clock,
        journal = mutableListOf<String>(),
    )
}
