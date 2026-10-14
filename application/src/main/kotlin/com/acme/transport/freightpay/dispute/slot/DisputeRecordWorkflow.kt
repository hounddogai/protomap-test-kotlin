package com.acme.transport.freightpay.dispute.slot

import com.acme.transport.freightpay.clock.Clock

/** Moves dispute records through their life cycle and keeps a journal of what it did. */
data class DisputeRecordCoordinator(
    var store: DisputeRecordStore,
    var validator: DisputeRecordValidator,
    var clock: Clock,
    var journal: MutableList<String>,
) {
    private fun note(message: String) {
        journal.add("${clock.now()} $message")
    }

    fun submit(code: String) {
        val item = store.find(code) ?: error("dispute record $code not found")
        validator.requireValid(item)
        item.moveTo(DisputeRecordStatus.WARMING)
        store.save(item)
        note("$code is warming")
    }

    fun retire(code: String, reason: String) {
        val item = store.find(code) ?: error("dispute record $code not found")
        if (item.isTerminal()) {
            note("$code already closed")
            return
        }
        item.moveTo(DisputeRecordStatus.STOPPED)
        store.save(item)
        note("$code is stopped: $reason")
    }

    fun reviewStale(cutoff: Int): Int {
        var changed = 0
        for (item in store.list()) {
            if (item.isTerminal()) {
                continue
            }
            if (item.startsAt > cutoff) {
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

fun newDisputeRecordCoordinator(store: DisputeRecordStore, clock: Clock): DisputeRecordCoordinator {
    return DisputeRecordCoordinator(
        store = store,
        validator = defaultDisputeRecordValidator(),
        clock = clock,
        journal = mutableListOf<String>(),
    )
}
