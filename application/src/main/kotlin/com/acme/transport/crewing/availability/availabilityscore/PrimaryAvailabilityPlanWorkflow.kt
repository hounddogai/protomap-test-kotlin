package com.acme.transport.crewing.availability.availabilityscore

import com.acme.transport.crewing.clock.Clock

/** Moves primary availability plans through their life cycle and keeps a journal of what it did. */
data class PrimaryAvailabilityPlanService(
    var store: PrimaryAvailabilityPlanStore,
    var validator: PrimaryAvailabilityPlanValidator,
    var clock: Clock,
    var journal: MutableList<String>,
) {
    private fun note(message: String) {
        journal.add("${clock.now()} $message")
    }

    fun advance(code: String) {
        val item = store.find(code) ?: error("primary availability plan $code not found")
        validator.requireValid(item)
        item.moveTo(PrimaryAvailabilityPlanStatus.SCHEDULED)
        store.save(item)
        note("$code is scheduled")
    }

    fun complete(code: String, reason: String) {
        val item = store.find(code) ?: error("primary availability plan $code not found")
        if (item.isTerminal()) {
            note("$code already closed")
            return
        }
        item.moveTo(PrimaryAvailabilityPlanStatus.CANCELLED)
        store.save(item)
        note("$code is cancelled: $reason")
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

fun newPrimaryAvailabilityPlanService(store: PrimaryAvailabilityPlanStore, clock: Clock): PrimaryAvailabilityPlanService {
    return PrimaryAvailabilityPlanService(
        store = store,
        validator = defaultPrimaryAvailabilityPlanValidator(),
        clock = clock,
        journal = mutableListOf<String>(),
    )
}
