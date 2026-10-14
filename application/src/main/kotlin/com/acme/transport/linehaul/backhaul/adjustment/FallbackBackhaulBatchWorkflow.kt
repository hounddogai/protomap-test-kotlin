package com.acme.transport.linehaul.backhaul.adjustment

import com.acme.transport.linehaul.clock.Clock

/** Moves fallback backhaul batches through their life cycle and keeps a journal of what it did. */
data class FallbackBackhaulBatchManager(
    var store: FallbackBackhaulBatchStore,
    var validator: FallbackBackhaulBatchValidator,
    var clock: Clock,
    var journal: MutableList<String>,
) {
    private fun note(message: String) {
        journal.add("${clock.now()} $message")
    }

    fun submit(code: String) {
        val item = store.find(code) ?: error("fallback backhaul batch $code not found")
        validator.requireValid(item)
        item.moveTo(FallbackBackhaulBatchStatus.SCHEDULED)
        store.save(item)
        note("$code is scheduled")
    }

    fun complete(code: String, reason: String) {
        val item = store.find(code) ?: error("fallback backhaul batch $code not found")
        if (item.isTerminal()) {
            note("$code already closed")
            return
        }
        item.moveTo(FallbackBackhaulBatchStatus.CANCELLED)
        store.save(item)
        note("$code is cancelled: $reason")
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

fun newFallbackBackhaulBatchManager(store: FallbackBackhaulBatchStore, clock: Clock): FallbackBackhaulBatchManager {
    return FallbackBackhaulBatchManager(
        store = store,
        validator = defaultFallbackBackhaulBatchValidator(),
        clock = clock,
        journal = mutableListOf<String>(),
    )
}
