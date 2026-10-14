package com.acme.transport.network.transittime.timenotice

import com.acme.transport.network.clock.Clock

/** Moves transit time adjustments through their life cycle and keeps a journal of what it did. */
data class TransitTimeAdjustmentDesk(
    var store: TransitTimeAdjustmentStore,
    var validator: TransitTimeAdjustmentValidator,
    var clock: Clock,
    var journal: MutableList<String>,
) {
    private fun note(message: String) {
        journal.add("${clock.now()} $message")
    }

    fun open(code: String) {
        val item = store.find(code) ?: error("transit time adjustment $code not found")
        validator.requireValid(item)
        item.moveTo(TransitTimeAdjustmentStatus.QUEUED)
        store.save(item)
        note("$code is queued")
    }

    fun close(code: String, reason: String) {
        val item = store.find(code) ?: error("transit time adjustment $code not found")
        if (item.isTerminal()) {
            note("$code already closed")
            return
        }
        item.moveTo(TransitTimeAdjustmentStatus.FAILED)
        store.save(item)
        note("$code is failed: $reason")
    }

    fun sweep(cutoff: Int): Int {
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

fun newTransitTimeAdjustmentDesk(store: TransitTimeAdjustmentStore, clock: Clock): TransitTimeAdjustmentDesk {
    return TransitTimeAdjustmentDesk(
        store = store,
        validator = defaultTransitTimeAdjustmentValidator(),
        clock = clock,
        journal = mutableListOf<String>(),
    )
}
