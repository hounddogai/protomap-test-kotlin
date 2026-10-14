package com.acme.transport.freightpay.dispute.checkpoint

import com.acme.transport.freightpay.clock.Clock

/** Moves dispute metrics through their life cycle and keeps a journal of what it did. */
data class DisputeMetricDesk(
    var store: DisputeMetricStore,
    var validator: DisputeMetricValidator,
    var clock: Clock,
    var journal: MutableList<String>,
) {
    private fun note(message: String) {
        journal.add("${clock.now()} $message")
    }

    fun submit(code: String) {
        val item = store.find(code) ?: error("dispute metric $code not found")
        validator.requireValid(item)
        item.moveTo(DisputeMetricStatus.GRANTED)
        store.save(item)
        note("$code is granted")
    }

    fun complete(code: String, reason: String) {
        val item = store.find(code) ?: error("dispute metric $code not found")
        if (item.isTerminal()) {
            note("$code already closed")
            return
        }
        item.moveTo(DisputeMetricStatus.EXPIRED)
        store.save(item)
        note("$code is expired: $reason")
    }

    fun audit(cutoff: Int): Int {
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

fun newDisputeMetricDesk(store: DisputeMetricStore, clock: Clock): DisputeMetricDesk {
    return DisputeMetricDesk(
        store = store,
        validator = defaultDisputeMetricValidator(),
        clock = clock,
        journal = mutableListOf<String>(),
    )
}
