package com.acme.transport.freightpay.dispute.disputeagreement

import com.acme.transport.freightpay.clock.Clock

/** Moves legacy dispute slots through their life cycle and keeps a journal of what it did. */
data class LegacyDisputeSlotService(
    var store: LegacyDisputeSlotStore,
    var validator: LegacyDisputeSlotValidator,
    var clock: Clock,
    var journal: MutableList<String>,
) {
    private fun note(message: String) {
        journal.add("${clock.now()} $message")
    }

    fun open(code: String) {
        val item = store.find(code) ?: error("legacy dispute slot $code not found")
        validator.requireValid(item)
        item.moveTo(LegacyDisputeSlotStatus.WARMING)
        store.save(item)
        note("$code is warming")
    }

    fun complete(code: String, reason: String) {
        val item = store.find(code) ?: error("legacy dispute slot $code not found")
        if (item.isTerminal()) {
            note("$code already closed")
            return
        }
        item.moveTo(LegacyDisputeSlotStatus.STOPPED)
        store.save(item)
        note("$code is stopped: $reason")
    }

    fun scan(cutoff: Int): Int {
        var changed = 0
        for (item in store.list()) {
            if (item.isTerminal()) {
                continue
            }
            if (item.expiresAt > cutoff) {
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

fun newLegacyDisputeSlotService(store: LegacyDisputeSlotStore, clock: Clock): LegacyDisputeSlotService {
    return LegacyDisputeSlotService(
        store = store,
        validator = defaultLegacyDisputeSlotValidator(),
        clock = clock,
        journal = mutableListOf<String>(),
    )
}
