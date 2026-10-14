package com.acme.transport.fleetops.leasereturn.returnforecast

import com.acme.transport.fleetops.clock.Clock

/** Moves primary lease return requests through their life cycle and keeps a journal of what it did. */
data class PrimaryLeaseReturnRequestDesk(
    var store: PrimaryLeaseReturnRequestStore,
    var validator: PrimaryLeaseReturnRequestValidator,
    var clock: Clock,
    var journal: MutableList<String>,
) {
    private fun note(message: String) {
        journal.add("${clock.now()} $message")
    }

    fun submit(code: String) {
        val item = store.find(code) ?: error("primary lease return request $code not found")
        validator.requireValid(item)
        item.moveTo(PrimaryLeaseReturnRequestStatus.PENDING)
        store.save(item)
        note("$code is pending")
    }

    fun finish(code: String, reason: String) {
        val item = store.find(code) ?: error("primary lease return request $code not found")
        if (item.isTerminal()) {
            note("$code already closed")
            return
        }
        item.moveTo(PrimaryLeaseReturnRequestStatus.CLOSED)
        store.save(item)
        note("$code is closed: $reason")
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

fun newPrimaryLeaseReturnRequestDesk(store: PrimaryLeaseReturnRequestStore, clock: Clock): PrimaryLeaseReturnRequestDesk {
    return PrimaryLeaseReturnRequestDesk(
        store = store,
        validator = defaultPrimaryLeaseReturnRequestValidator(),
        clock = clock,
        journal = mutableListOf<String>(),
    )
}
