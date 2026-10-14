package com.acme.transport.freightpay.dispute.request

import com.acme.transport.freightpay.clock.Clock

/** Moves manual dispute requests through their life cycle and keeps a journal of what it did. */
data class ManualDisputeRequestService(
    var store: ManualDisputeRequestStore,
    var validator: ManualDisputeRequestValidator,
    var clock: Clock,
    var journal: MutableList<String>,
) {
    private fun note(message: String) {
        journal.add("${clock.now()} $message")
    }

    fun start(code: String) {
        val item = store.find(code) ?: error("manual dispute request $code not found")
        validator.requireValid(item)
        item.moveTo(ManualDisputeRequestStatus.QUEUED)
        store.save(item)
        note("$code is queued")
    }

    fun complete(code: String, reason: String) {
        val item = store.find(code) ?: error("manual dispute request $code not found")
        if (item.isTerminal()) {
            note("$code already closed")
            return
        }
        item.moveTo(ManualDisputeRequestStatus.FAILED)
        store.save(item)
        note("$code is failed: $reason")
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

fun newManualDisputeRequestService(store: ManualDisputeRequestStore, clock: Clock): ManualDisputeRequestService {
    return ManualDisputeRequestService(
        store = store,
        validator = defaultManualDisputeRequestValidator(),
        clock = clock,
        journal = mutableListOf<String>(),
    )
}
