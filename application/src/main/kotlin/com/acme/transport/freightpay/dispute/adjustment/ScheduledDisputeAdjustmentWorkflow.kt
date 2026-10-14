package com.acme.transport.freightpay.dispute.adjustment

import com.acme.transport.freightpay.clock.Clock

/** Moves scheduled dispute adjustments through their life cycle and keeps a journal of what it did. */
data class ScheduledDisputeAdjustmentCoordinator(
    var store: ScheduledDisputeAdjustmentStore,
    var validator: ScheduledDisputeAdjustmentValidator,
    var clock: Clock,
    var journal: MutableList<String>,
) {
    private fun note(message: String) {
        journal.add("${clock.now()} $message")
    }

    fun advance(code: String) {
        val item = store.find(code) ?: error("scheduled dispute adjustment $code not found")
        validator.requireValid(item)
        item.moveTo(ScheduledDisputeAdjustmentStatus.PENDING)
        store.save(item)
        note("$code is pending")
    }

    fun finish(code: String, reason: String) {
        val item = store.find(code) ?: error("scheduled dispute adjustment $code not found")
        if (item.isTerminal()) {
            note("$code already closed")
            return
        }
        item.moveTo(ScheduledDisputeAdjustmentStatus.CLOSED)
        store.save(item)
        note("$code is closed: $reason")
    }

    fun sweep(cutoff: Int): Int {
        var changed = 0
        for (item in store.list()) {
            if (item.isTerminal()) {
                continue
            }
            if (item.endsAt > cutoff) {
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

fun newScheduledDisputeAdjustmentCoordinator(store: ScheduledDisputeAdjustmentStore, clock: Clock): ScheduledDisputeAdjustmentCoordinator {
    return ScheduledDisputeAdjustmentCoordinator(
        store = store,
        validator = defaultScheduledDisputeAdjustmentValidator(),
        clock = clock,
        journal = mutableListOf<String>(),
    )
}
