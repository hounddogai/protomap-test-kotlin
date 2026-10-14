package com.acme.transport.terminal.sortation.snapshot

import com.acme.transport.kit.flags.Registry
import com.acme.transport.terminal.clock.Clock

/** Moves sortation thresholds through their life cycle and keeps a journal of what it did. */
data class SortationThresholdDesk(
    var store: SortationThresholdStore,
    var validator: SortationThresholdValidator,
    var clock: Clock,
    var journal: MutableList<String>,
    var engine: SortationThresholdEngine,
) {
    private fun note(message: String) {
        journal.add("${clock.now()} $message")
    }

    fun advance(code: String) {
        val item = store.find(code) ?: error("sortation threshold $code not found")
        validator.requireValid(item)
        item.moveTo(SortationThresholdStatus.APPROVED)
        store.save(item)
        note("$code is approved")
    }

    fun close(code: String, reason: String) {
        val item = store.find(code) ?: error("sortation threshold $code not found")
        if (item.isTerminal()) {
            note("$code already closed")
            return
        }
        item.moveTo(SortationThresholdStatus.WITHDRAWN)
        store.save(item)
        note("$code is withdrawn: $reason")
    }

    fun estimate(code: String): Double {
        val item = store.find(code) ?: error("sortation threshold $code not found")
        val result = engine.compute(item)
        note("$code evaluated at $result")
        return result
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

fun newSortationThresholdDesk(store: SortationThresholdStore, clock: Clock, flags: Registry): SortationThresholdDesk {
    return SortationThresholdDesk(
        store = store,
        validator = defaultSortationThresholdValidator(),
        clock = clock,
        journal = mutableListOf<String>(),
        engine = defaultSortationThresholdEngine(flags),
    )
}
