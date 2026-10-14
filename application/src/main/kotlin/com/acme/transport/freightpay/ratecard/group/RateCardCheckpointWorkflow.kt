package com.acme.transport.freightpay.ratecard.group

import com.acme.transport.freightpay.clock.Clock

/** Moves rate card checkpoints through their life cycle and keeps a journal of what it did. */
data class RateCardCheckpointService(
    var store: RateCardCheckpointStore,
    var validator: RateCardCheckpointValidator,
    var clock: Clock,
    var journal: MutableList<String>,
) {
    private fun note(message: String) {
        journal.add("${clock.now()} $message")
    }

    fun submit(code: String) {
        val item = store.find(code) ?: error("rate card checkpoint $code not found")
        validator.requireValid(item)
        item.moveTo(RateCardCheckpointStatus.APPROVED)
        store.save(item)
        note("$code is approved")
    }

    fun close(code: String, reason: String) {
        val item = store.find(code) ?: error("rate card checkpoint $code not found")
        if (item.isTerminal()) {
            note("$code already closed")
            return
        }
        item.moveTo(RateCardCheckpointStatus.WITHDRAWN)
        store.save(item)
        note("$code is withdrawn: $reason")
    }

    fun reviewStale(cutoff: Int): Int {
        var changed = 0
        for (item in store.list()) {
            if (item.isTerminal()) {
                continue
            }
            if (item.createdAt > cutoff) {
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

fun newRateCardCheckpointService(store: RateCardCheckpointStore, clock: Clock): RateCardCheckpointService {
    return RateCardCheckpointService(
        store = store,
        validator = defaultRateCardCheckpointValidator(),
        clock = clock,
        journal = mutableListOf<String>(),
    )
}
