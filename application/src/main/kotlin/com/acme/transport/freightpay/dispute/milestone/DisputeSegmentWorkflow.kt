package com.acme.transport.freightpay.dispute.milestone

import com.acme.transport.freightpay.clock.Clock

/** Moves dispute segments through their life cycle and keeps a journal of what it did. */
data class DisputeSegmentDesk(
    var store: DisputeSegmentStore,
    var validator: DisputeSegmentValidator,
    var clock: Clock,
    var journal: MutableList<String>,
) {
    private fun note(message: String) {
        journal.add("${clock.now()} $message")
    }

    fun submit(code: String) {
        val item = store.find(code) ?: error("dispute segment $code not found")
        validator.requireValid(item)
        item.moveTo(DisputeSegmentStatus.WARMING)
        store.save(item)
        note("$code is warming")
    }

    fun complete(code: String, reason: String) {
        val item = store.find(code) ?: error("dispute segment $code not found")
        if (item.isTerminal()) {
            note("$code already closed")
            return
        }
        item.moveTo(DisputeSegmentStatus.STOPPED)
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

fun newDisputeSegmentDesk(store: DisputeSegmentStore, clock: Clock): DisputeSegmentDesk {
    return DisputeSegmentDesk(
        store = store,
        validator = defaultDisputeSegmentValidator(),
        clock = clock,
        journal = mutableListOf<String>(),
    )
}
