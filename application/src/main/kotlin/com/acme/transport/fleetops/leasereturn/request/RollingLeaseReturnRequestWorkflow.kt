package com.acme.transport.fleetops.leasereturn.request

import com.acme.transport.fleetops.clock.Clock

/** Moves rolling lease return requests through their life cycle and keeps a journal of what it did. */
data class RollingLeaseReturnRequestCoordinator(
    var store: RollingLeaseReturnRequestStore,
    var validator: RollingLeaseReturnRequestValidator,
    var clock: Clock,
    var journal: MutableList<String>,
) {
    private fun note(message: String) {
        journal.add("${clock.now()} $message")
    }

    fun open(code: String) {
        val item = store.find(code) ?: error("rolling lease return request $code not found")
        validator.requireValid(item)
        item.moveTo(RollingLeaseReturnRequestStatus.IN_REVIEW)
        store.save(item)
        note("$code is in review")
    }

    fun settle(code: String, reason: String) {
        val item = store.find(code) ?: error("rolling lease return request $code not found")
        if (item.isTerminal()) {
            note("$code already closed")
            return
        }
        item.moveTo(RollingLeaseReturnRequestStatus.ARCHIVED)
        store.save(item)
        note("$code is archived: $reason")
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

fun newRollingLeaseReturnRequestCoordinator(store: RollingLeaseReturnRequestStore, clock: Clock): RollingLeaseReturnRequestCoordinator {
    return RollingLeaseReturnRequestCoordinator(
        store = store,
        validator = defaultRollingLeaseReturnRequestValidator(),
        clock = clock,
        journal = mutableListOf<String>(),
    )
}
