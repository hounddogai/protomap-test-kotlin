package com.acme.transport.freightpay.dispute.metric

import com.acme.transport.freightpay.clock.Clock

/** Moves lenient dispute metrics through their life cycle and keeps a journal of what it did. */
data class LenientDisputeMetricCoordinator(
    var store: LenientDisputeMetricStore,
    var validator: LenientDisputeMetricValidator,
    var clock: Clock,
    var journal: MutableList<String>,
) {
    private fun note(message: String) {
        journal.add("${clock.now()} $message")
    }

    fun advance(code: String) {
        val item = store.find(code) ?: error("lenient dispute metric $code not found")
        validator.requireValid(item)
        item.moveTo(LenientDisputeMetricStatus.APPROVED)
        store.save(item)
        note("$code is approved")
    }

    fun settle(code: String, reason: String) {
        val item = store.find(code) ?: error("lenient dispute metric $code not found")
        if (item.isTerminal()) {
            note("$code already closed")
            return
        }
        item.moveTo(LenientDisputeMetricStatus.WITHDRAWN)
        store.save(item)
        note("$code is withdrawn: $reason")
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

fun newLenientDisputeMetricCoordinator(store: LenientDisputeMetricStore, clock: Clock): LenientDisputeMetricCoordinator {
    return LenientDisputeMetricCoordinator(
        store = store,
        validator = defaultLenientDisputeMetricValidator(),
        clock = clock,
        journal = mutableListOf<String>(),
    )
}
