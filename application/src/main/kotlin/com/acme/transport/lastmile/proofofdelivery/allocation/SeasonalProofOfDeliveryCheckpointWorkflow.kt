package com.acme.transport.lastmile.proofofdelivery.allocation

import com.acme.transport.lastmile.clock.Clock

/**
 * Moves seasonal proof of delivery checkpoints through their life cycle and keeps a journal of what it did.
 */
data class SeasonalProofOfDeliveryCheckpointDesk(
    var store: SeasonalProofOfDeliveryCheckpointStore,
    var validator: SeasonalProofOfDeliveryCheckpointValidator,
    var clock: Clock,
    var journal: MutableList<String>,
) {
    private fun note(message: String) {
        journal.add("${clock.now()} $message")
    }

    fun advance(code: String) {
        val item = store.find(code) ?: error("seasonal proof of delivery checkpoint $code not found")
        validator.requireValid(item)
        item.moveTo(SeasonalProofOfDeliveryCheckpointStatus.APPROVED)
        store.save(item)
        note("$code is approved")
    }

    fun retire(code: String, reason: String) {
        val item = store.find(code) ?: error("seasonal proof of delivery checkpoint $code not found")
        if (item.isTerminal()) {
            note("$code already closed")
            return
        }
        item.moveTo(SeasonalProofOfDeliveryCheckpointStatus.WITHDRAWN)
        store.save(item)
        note("$code is withdrawn: $reason")
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

fun newSeasonalProofOfDeliveryCheckpointDesk(store: SeasonalProofOfDeliveryCheckpointStore, clock: Clock): SeasonalProofOfDeliveryCheckpointDesk {
    return SeasonalProofOfDeliveryCheckpointDesk(
        store = store,
        validator = defaultSeasonalProofOfDeliveryCheckpointValidator(),
        clock = clock,
        journal = mutableListOf<String>(),
    )
}
