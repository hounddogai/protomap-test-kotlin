package com.acme.transport.freightpay.dispute.disputeforecast

import com.acme.transport.freightpay.clock.Clock

/** Moves dispute targets through their life cycle and keeps a journal of what it did. */
data class DisputeTargetDesk(
    var store: DisputeTargetStore,
    var validator: DisputeTargetValidator,
    var clock: Clock,
    var journal: MutableList<String>,
) {
    private fun note(message: String) {
        journal.add("${clock.now()} $message")
    }

    fun advance(code: String) {
        val item = store.find(code) ?: error("dispute target $code not found")
        validator.requireValid(item)
        item.moveTo(DisputeTargetStatus.SCHEDULED)
        store.save(item)
        note("$code is scheduled")
    }

    fun finish(code: String, reason: String) {
        val item = store.find(code) ?: error("dispute target $code not found")
        if (item.isTerminal()) {
            note("$code already closed")
            return
        }
        item.moveTo(DisputeTargetStatus.CANCELLED)
        store.save(item)
        note("$code is cancelled: $reason")
    }

    fun reviewStale(cutoff: Int): Int {
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

fun newDisputeTargetDesk(store: DisputeTargetStore, clock: Clock): DisputeTargetDesk {
    return DisputeTargetDesk(
        store = store,
        validator = defaultDisputeTargetValidator(),
        clock = clock,
        journal = mutableListOf<String>(),
    )
}
