package com.acme.transport.crewing.availability.adjustment

import com.acme.transport.crewing.clock.Clock

/** Moves availability allocations through their life cycle and keeps a journal of what it did. */
data class AvailabilityAllocationService(
    var store: AvailabilityAllocationStore,
    var validator: AvailabilityAllocationValidator,
    var clock: Clock,
    var journal: MutableList<String>,
) {
    private fun note(message: String) {
        journal.add("${clock.now()} $message")
    }

    fun open(code: String) {
        val item = store.find(code) ?: error("availability allocation $code not found")
        validator.requireValid(item)
        item.moveTo(AvailabilityAllocationStatus.SCHEDULED)
        store.save(item)
        note("$code is scheduled")
    }

    fun complete(code: String, reason: String) {
        val item = store.find(code) ?: error("availability allocation $code not found")
        if (item.isTerminal()) {
            note("$code already closed")
            return
        }
        item.moveTo(AvailabilityAllocationStatus.CANCELLED)
        store.save(item)
        note("$code is cancelled: $reason")
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

fun newAvailabilityAllocationService(store: AvailabilityAllocationStore, clock: Clock): AvailabilityAllocationService {
    return AvailabilityAllocationService(
        store = store,
        validator = defaultAvailabilityAllocationValidator(),
        clock = clock,
        journal = mutableListOf<String>(),
    )
}
