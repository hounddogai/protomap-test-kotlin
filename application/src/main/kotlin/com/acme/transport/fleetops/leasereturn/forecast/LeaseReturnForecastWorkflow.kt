package com.acme.transport.fleetops.leasereturn.forecast

import com.acme.transport.fleetops.clock.Clock

/** Moves lease return forecasts through their life cycle and keeps a journal of what it did. */
data class LeaseReturnForecastCoordinator(
    var store: LeaseReturnForecastStore,
    var validator: LeaseReturnForecastValidator,
    var clock: Clock,
    var journal: MutableList<String>,
) {
    private fun note(message: String) {
        journal.add("${clock.now()} $message")
    }

    fun advance(code: String) {
        val item = store.find(code) ?: error("lease return forecast $code not found")
        validator.requireValid(item)
        item.moveTo(LeaseReturnForecastStatus.APPROVED)
        store.save(item)
        note("$code is approved")
    }

    fun complete(code: String, reason: String) {
        val item = store.find(code) ?: error("lease return forecast $code not found")
        if (item.isTerminal()) {
            note("$code already closed")
            return
        }
        item.moveTo(LeaseReturnForecastStatus.WITHDRAWN)
        store.save(item)
        note("$code is withdrawn: $reason")
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

fun newLeaseReturnForecastCoordinator(store: LeaseReturnForecastStore, clock: Clock): LeaseReturnForecastCoordinator {
    return LeaseReturnForecastCoordinator(
        store = store,
        validator = defaultLeaseReturnForecastValidator(),
        clock = clock,
        journal = mutableListOf<String>(),
    )
}
