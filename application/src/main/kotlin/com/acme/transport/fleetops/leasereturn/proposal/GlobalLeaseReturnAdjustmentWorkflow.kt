package com.acme.transport.fleetops.leasereturn.proposal

import com.acme.transport.fleetops.clock.Clock

/**
 * Moves global lease return adjustments through their life cycle and keeps a journal of what it did.
 */
data class GlobalLeaseReturnAdjustmentCoordinator(
    var store: GlobalLeaseReturnAdjustmentStore,
    var validator: GlobalLeaseReturnAdjustmentValidator,
    var clock: Clock,
    var journal: MutableList<String>,
) {
    private fun note(message: String) {
        journal.add("${clock.now()} $message")
    }

    fun open(code: String) {
        val item = store.find(code) ?: error("global lease return adjustment $code not found")
        validator.requireValid(item)
        item.moveTo(GlobalLeaseReturnAdjustmentStatus.APPROVED)
        store.save(item)
        note("$code is approved")
    }

    fun close(code: String, reason: String) {
        val item = store.find(code) ?: error("global lease return adjustment $code not found")
        if (item.isTerminal()) {
            note("$code already closed")
            return
        }
        item.moveTo(GlobalLeaseReturnAdjustmentStatus.WITHDRAWN)
        store.save(item)
        note("$code is withdrawn: $reason")
    }

    fun scan(cutoff: Int): Int {
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

fun newGlobalLeaseReturnAdjustmentCoordinator(store: GlobalLeaseReturnAdjustmentStore, clock: Clock): GlobalLeaseReturnAdjustmentCoordinator {
    return GlobalLeaseReturnAdjustmentCoordinator(
        store = store,
        validator = defaultGlobalLeaseReturnAdjustmentValidator(),
        clock = clock,
        journal = mutableListOf<String>(),
    )
}
