package com.acme.transport.fleetops.leasereturn.forecast

import com.acme.transport.fleetops.clock.Clock

/**
 * Moves weighted lease return profiles through their life cycle and keeps a journal of what it did.
 */
data class WeightedLeaseReturnProfileCoordinator(
    var store: WeightedLeaseReturnProfileStore,
    var validator: WeightedLeaseReturnProfileValidator,
    var clock: Clock,
    var journal: MutableList<String>,
) {
    private fun note(message: String) {
        journal.add("${clock.now()} $message")
    }

    fun open(code: String) {
        val item = store.find(code) ?: error("weighted lease return profile $code not found")
        validator.requireValid(item)
        item.moveTo(WeightedLeaseReturnProfileStatus.SCHEDULED)
        store.save(item)
        note("$code is scheduled")
    }

    fun retire(code: String, reason: String) {
        val item = store.find(code) ?: error("weighted lease return profile $code not found")
        if (item.isTerminal()) {
            note("$code already closed")
            return
        }
        item.moveTo(WeightedLeaseReturnProfileStatus.CANCELLED)
        store.save(item)
        note("$code is cancelled: $reason")
    }

    fun sweep(cutoff: Int): Int {
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

fun newWeightedLeaseReturnProfileCoordinator(store: WeightedLeaseReturnProfileStore, clock: Clock): WeightedLeaseReturnProfileCoordinator {
    return WeightedLeaseReturnProfileCoordinator(
        store = store,
        validator = defaultWeightedLeaseReturnProfileValidator(),
        clock = clock,
        journal = mutableListOf<String>(),
    )
}
