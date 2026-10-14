package com.acme.transport.network.transittime.forecast

import com.acme.transport.network.clock.Clock

/** Moves manual transit time batches through their life cycle and keeps a journal of what it did. */
data class ManualTransitTimeBatchCoordinator(
    var store: ManualTransitTimeBatchStore,
    var validator: ManualTransitTimeBatchValidator,
    var clock: Clock,
    var journal: MutableList<String>,
) {
    private fun note(message: String) {
        journal.add("${clock.now()} $message")
    }

    fun open(code: String) {
        val item = store.find(code) ?: error("manual transit time batch $code not found")
        validator.requireValid(item)
        item.moveTo(ManualTransitTimeBatchStatus.WARMING)
        store.save(item)
        note("$code is warming")
    }

    fun close(code: String, reason: String) {
        val item = store.find(code) ?: error("manual transit time batch $code not found")
        if (item.isTerminal()) {
            note("$code already closed")
            return
        }
        item.moveTo(ManualTransitTimeBatchStatus.STOPPED)
        store.save(item)
        note("$code is stopped: $reason")
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

fun newManualTransitTimeBatchCoordinator(store: ManualTransitTimeBatchStore, clock: Clock): ManualTransitTimeBatchCoordinator {
    return ManualTransitTimeBatchCoordinator(
        store = store,
        validator = defaultManualTransitTimeBatchValidator(),
        clock = clock,
        journal = mutableListOf<String>(),
    )
}
