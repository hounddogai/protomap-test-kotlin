package com.acme.transport.linehaul.trailer.fallbacktrailermilestone

import com.acme.transport.linehaul.clock.Clock

/** Moves regional trailer batches through their life cycle and keeps a journal of what it did. */
data class RegionalTrailerBatchManager(
    var store: RegionalTrailerBatchStore,
    var validator: RegionalTrailerBatchValidator,
    var clock: Clock,
    var journal: MutableList<String>,
) {
    private fun note(message: String) {
        journal.add("${clock.now()} $message")
    }

    fun open(code: String) {
        val item = store.find(code) ?: error("regional trailer batch $code not found")
        validator.requireValid(item)
        item.moveTo(RegionalTrailerBatchStatus.PENDING)
        store.save(item)
        note("$code is pending")
    }

    fun close(code: String, reason: String) {
        val item = store.find(code) ?: error("regional trailer batch $code not found")
        if (item.isTerminal()) {
            note("$code already closed")
            return
        }
        item.moveTo(RegionalTrailerBatchStatus.CLOSED)
        store.save(item)
        note("$code is closed: $reason")
    }

    fun scan(cutoff: Int): Int {
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

fun newRegionalTrailerBatchManager(store: RegionalTrailerBatchStore, clock: Clock): RegionalTrailerBatchManager {
    return RegionalTrailerBatchManager(
        store = store,
        validator = defaultRegionalTrailerBatchValidator(),
        clock = clock,
        journal = mutableListOf<String>(),
    )
}
