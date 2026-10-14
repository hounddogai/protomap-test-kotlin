package com.acme.transport.customs.clearance.quota

import com.acme.transport.customs.clock.Clock

/** Moves pending clearance allocations through their life cycle and keeps a journal of what it did. */
data class PendingClearanceAllocationService(
    var store: PendingClearanceAllocationStore,
    var validator: PendingClearanceAllocationValidator,
    var clock: Clock,
    var journal: MutableList<String>,
) {
    private fun note(message: String) {
        journal.add("${clock.now()} $message")
    }

    fun start(code: String) {
        val item = store.find(code) ?: error("pending clearance allocation $code not found")
        validator.requireValid(item)
        item.moveTo(PendingClearanceAllocationStatus.GRANTED)
        store.save(item)
        note("$code is granted")
    }

    fun settle(code: String, reason: String) {
        val item = store.find(code) ?: error("pending clearance allocation $code not found")
        if (item.isTerminal()) {
            note("$code already closed")
            return
        }
        item.moveTo(PendingClearanceAllocationStatus.EXPIRED)
        store.save(item)
        note("$code is expired: $reason")
    }

    fun audit(cutoff: Int): Int {
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

fun newPendingClearanceAllocationService(store: PendingClearanceAllocationStore, clock: Clock): PendingClearanceAllocationService {
    return PendingClearanceAllocationService(
        store = store,
        validator = defaultPendingClearanceAllocationValidator(),
        clock = clock,
        journal = mutableListOf<String>(),
    )
}
