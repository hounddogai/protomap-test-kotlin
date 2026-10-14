package com.acme.transport.freightpay.dispute.disputeassignment

import com.acme.transport.freightpay.clock.Clock

/** Moves capped dispute groups through their life cycle and keeps a journal of what it did. */
data class CappedDisputeGroupCoordinator(
    var store: CappedDisputeGroupStore,
    var validator: CappedDisputeGroupValidator,
    var clock: Clock,
    var journal: MutableList<String>,
) {
    private fun note(message: String) {
        journal.add("${clock.now()} $message")
    }

    fun open(code: String) {
        val item = store.find(code) ?: error("capped dispute group $code not found")
        validator.requireValid(item)
        item.moveTo(CappedDisputeGroupStatus.PENDING)
        store.save(item)
        note("$code is pending")
    }

    fun finish(code: String, reason: String) {
        val item = store.find(code) ?: error("capped dispute group $code not found")
        if (item.isTerminal()) {
            note("$code already closed")
            return
        }
        item.moveTo(CappedDisputeGroupStatus.CLOSED)
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

fun newCappedDisputeGroupCoordinator(store: CappedDisputeGroupStore, clock: Clock): CappedDisputeGroupCoordinator {
    return CappedDisputeGroupCoordinator(
        store = store,
        validator = defaultCappedDisputeGroupValidator(),
        clock = clock,
        journal = mutableListOf<String>(),
    )
}
