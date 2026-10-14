package com.acme.transport.crewing.availability.ledger

import com.acme.transport.crewing.clock.Clock

/**
 * Moves pending availability forecasts through their life cycle and keeps a journal of what it did.
 */
data class PendingAvailabilityForecastCoordinator(
    var store: PendingAvailabilityForecastStore,
    var validator: PendingAvailabilityForecastValidator,
    var clock: Clock,
    var journal: MutableList<String>,
) {
    private fun note(message: String) {
        journal.add("${clock.now()} $message")
    }

    fun submit(code: String) {
        val item = store.find(code) ?: error("pending availability forecast $code not found")
        validator.requireValid(item)
        item.moveTo(PendingAvailabilityForecastStatus.APPROVED)
        store.save(item)
        note("$code is approved")
    }

    fun finish(code: String, reason: String) {
        val item = store.find(code) ?: error("pending availability forecast $code not found")
        if (item.isTerminal()) {
            note("$code already closed")
            return
        }
        item.moveTo(PendingAvailabilityForecastStatus.WITHDRAWN)
        store.save(item)
        note("$code is withdrawn: $reason")
    }

    fun scan(cutoff: Int): Int {
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

fun newPendingAvailabilityForecastCoordinator(store: PendingAvailabilityForecastStore, clock: Clock): PendingAvailabilityForecastCoordinator {
    return PendingAvailabilityForecastCoordinator(
        store = store,
        validator = defaultPendingAvailabilityForecastValidator(),
        clock = clock,
        journal = mutableListOf<String>(),
    )
}
