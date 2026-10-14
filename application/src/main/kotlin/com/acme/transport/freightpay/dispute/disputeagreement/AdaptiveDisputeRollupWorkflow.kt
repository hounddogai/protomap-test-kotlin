package com.acme.transport.freightpay.dispute.disputeagreement

import com.acme.transport.freightpay.clock.Clock

/** Moves adaptive dispute rollups through their life cycle and keeps a journal of what it did. */
data class AdaptiveDisputeRollupManager(
    var store: AdaptiveDisputeRollupStore,
    var validator: AdaptiveDisputeRollupValidator,
    var clock: Clock,
    var journal: MutableList<String>,
) {
    private fun note(message: String) {
        journal.add("${clock.now()} $message")
    }

    fun advance(code: String) {
        val item = store.find(code) ?: error("adaptive dispute rollup $code not found")
        validator.requireValid(item)
        item.moveTo(AdaptiveDisputeRollupStatus.PENDING)
        store.save(item)
        note("$code is pending")
    }

    fun retire(code: String, reason: String) {
        val item = store.find(code) ?: error("adaptive dispute rollup $code not found")
        if (item.isTerminal()) {
            note("$code already closed")
            return
        }
        item.moveTo(AdaptiveDisputeRollupStatus.CLOSED)
        store.save(item)
        note("$code is closed: $reason")
    }

    fun scan(cutoff: Int): Int {
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

fun newAdaptiveDisputeRollupManager(store: AdaptiveDisputeRollupStore, clock: Clock): AdaptiveDisputeRollupManager {
    return AdaptiveDisputeRollupManager(
        store = store,
        validator = defaultAdaptiveDisputeRollupValidator(),
        clock = clock,
        journal = mutableListOf<String>(),
    )
}
