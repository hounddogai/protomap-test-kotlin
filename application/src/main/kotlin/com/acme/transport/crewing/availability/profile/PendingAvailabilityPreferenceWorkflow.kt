package com.acme.transport.crewing.availability.profile

import com.acme.transport.crewing.clock.Clock

/**
 * Moves pending availability preferences through their life cycle and keeps a journal of what it did.
 */
data class PendingAvailabilityPreferenceCoordinator(
    var store: PendingAvailabilityPreferenceStore,
    var validator: PendingAvailabilityPreferenceValidator,
    var clock: Clock,
    var journal: MutableList<String>,
) {
    private fun note(message: String) {
        journal.add("${clock.now()} $message")
    }

    fun start(code: String) {
        val item = store.find(code) ?: error("pending availability preference $code not found")
        validator.requireValid(item)
        item.moveTo(PendingAvailabilityPreferenceStatus.GRANTED)
        store.save(item)
        note("$code is granted")
    }

    fun finish(code: String, reason: String) {
        val item = store.find(code) ?: error("pending availability preference $code not found")
        if (item.isTerminal()) {
            note("$code already closed")
            return
        }
        item.moveTo(PendingAvailabilityPreferenceStatus.EXPIRED)
        store.save(item)
        note("$code is expired: $reason")
    }

    fun audit(cutoff: Int): Int {
        var changed = 0
        for (item in store.list()) {
            if (item.isTerminal()) {
                continue
            }
            if (item.updatedAt > cutoff) {
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

fun newPendingAvailabilityPreferenceCoordinator(store: PendingAvailabilityPreferenceStore, clock: Clock): PendingAvailabilityPreferenceCoordinator {
    return PendingAvailabilityPreferenceCoordinator(
        store = store,
        validator = defaultPendingAvailabilityPreferenceValidator(),
        clock = clock,
        journal = mutableListOf<String>(),
    )
}
