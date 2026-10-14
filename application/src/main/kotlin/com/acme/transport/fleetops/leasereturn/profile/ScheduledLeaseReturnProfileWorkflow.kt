package com.acme.transport.fleetops.leasereturn.profile

import com.acme.transport.fleetops.clock.Clock

/**
 * Moves scheduled lease return profiles through their life cycle and keeps a journal of what it did.
 */
data class ScheduledLeaseReturnProfileDesk(
    var store: ScheduledLeaseReturnProfileStore,
    var validator: ScheduledLeaseReturnProfileValidator,
    var clock: Clock,
    var journal: MutableList<String>,
) {
    private fun note(message: String) {
        journal.add("${clock.now()} $message")
    }

    fun start(code: String) {
        val item = store.find(code) ?: error("scheduled lease return profile $code not found")
        validator.requireValid(item)
        item.moveTo(ScheduledLeaseReturnProfileStatus.WARMING)
        store.save(item)
        note("$code is warming")
    }

    fun settle(code: String, reason: String) {
        val item = store.find(code) ?: error("scheduled lease return profile $code not found")
        if (item.isTerminal()) {
            note("$code already closed")
            return
        }
        item.moveTo(ScheduledLeaseReturnProfileStatus.STOPPED)
        store.save(item)
        note("$code is stopped: $reason")
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

fun newScheduledLeaseReturnProfileDesk(store: ScheduledLeaseReturnProfileStore, clock: Clock): ScheduledLeaseReturnProfileDesk {
    return ScheduledLeaseReturnProfileDesk(
        store = store,
        validator = defaultScheduledLeaseReturnProfileValidator(),
        clock = clock,
        journal = mutableListOf<String>(),
    )
}
