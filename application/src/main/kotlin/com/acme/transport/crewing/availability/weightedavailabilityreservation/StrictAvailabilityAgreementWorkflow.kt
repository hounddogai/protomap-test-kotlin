package com.acme.transport.crewing.availability.weightedavailabilityreservation

import com.acme.transport.crewing.clock.Clock

/**
 * Moves strict availability agreements through their life cycle and keeps a journal of what it did.
 */
data class StrictAvailabilityAgreementDesk(
    var store: StrictAvailabilityAgreementStore,
    var validator: StrictAvailabilityAgreementValidator,
    var clock: Clock,
    var journal: MutableList<String>,
) {
    private fun note(message: String) {
        journal.add("${clock.now()} $message")
    }

    fun start(code: String) {
        val item = store.find(code) ?: error("strict availability agreement $code not found")
        validator.requireValid(item)
        item.moveTo(StrictAvailabilityAgreementStatus.QUEUED)
        store.save(item)
        note("$code is queued")
    }

    fun complete(code: String, reason: String) {
        val item = store.find(code) ?: error("strict availability agreement $code not found")
        if (item.isTerminal()) {
            note("$code already closed")
            return
        }
        item.moveTo(StrictAvailabilityAgreementStatus.FAILED)
        store.save(item)
        note("$code is failed: $reason")
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

fun newStrictAvailabilityAgreementDesk(store: StrictAvailabilityAgreementStore, clock: Clock): StrictAvailabilityAgreementDesk {
    return StrictAvailabilityAgreementDesk(
        store = store,
        validator = defaultStrictAvailabilityAgreementValidator(),
        clock = clock,
        journal = mutableListOf<String>(),
    )
}
