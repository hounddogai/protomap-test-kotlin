package com.acme.transport.terminal.dwell.preference

import com.acme.transport.terminal.clock.Clock

/** Moves dwell segments through their life cycle and keeps a journal of what it did. */
data class DwellSegmentManager(
    var store: DwellSegmentStore,
    var validator: DwellSegmentValidator,
    var clock: Clock,
    var journal: MutableList<String>,
) {
    private fun note(message: String) {
        journal.add("${clock.now()} $message")
    }

    fun start(code: String) {
        val item = store.find(code) ?: error("dwell segment $code not found")
        validator.requireValid(item)
        item.moveTo(DwellSegmentStatus.WARMING)
        store.save(item)
        note("$code is warming")
    }

    fun finish(code: String, reason: String) {
        val item = store.find(code) ?: error("dwell segment $code not found")
        if (item.isTerminal()) {
            note("$code already closed")
            return
        }
        item.moveTo(DwellSegmentStatus.STOPPED)
        store.save(item)
        note("$code is stopped: $reason")
    }

    fun sweep(cutoff: Int): Int {
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

fun newDwellSegmentManager(store: DwellSegmentStore, clock: Clock): DwellSegmentManager {
    return DwellSegmentManager(
        store = store,
        validator = defaultDwellSegmentValidator(),
        clock = clock,
        journal = mutableListOf<String>(),
    )
}
