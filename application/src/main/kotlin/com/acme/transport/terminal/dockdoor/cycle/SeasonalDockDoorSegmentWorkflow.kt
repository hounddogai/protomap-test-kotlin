package com.acme.transport.terminal.dockdoor.cycle

import com.acme.transport.terminal.clock.Clock

/** Moves seasonal dock door segments through their life cycle and keeps a journal of what it did. */
data class SeasonalDockDoorSegmentManager(
    var store: SeasonalDockDoorSegmentStore,
    var validator: SeasonalDockDoorSegmentValidator,
    var clock: Clock,
    var journal: MutableList<String>,
) {
    private fun note(message: String) {
        journal.add("${clock.now()} $message")
    }

    fun start(code: String) {
        val item = store.find(code) ?: error("seasonal dock door segment $code not found")
        validator.requireValid(item)
        item.moveTo(SeasonalDockDoorSegmentStatus.WARMING)
        store.save(item)
        note("$code is warming")
    }

    fun complete(code: String, reason: String) {
        val item = store.find(code) ?: error("seasonal dock door segment $code not found")
        if (item.isTerminal()) {
            note("$code already closed")
            return
        }
        item.moveTo(SeasonalDockDoorSegmentStatus.STOPPED)
        store.save(item)
        note("$code is stopped: $reason")
    }

    fun sweep(cutoff: Int): Int {
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

fun newSeasonalDockDoorSegmentManager(store: SeasonalDockDoorSegmentStore, clock: Clock): SeasonalDockDoorSegmentManager {
    return SeasonalDockDoorSegmentManager(
        store = store,
        validator = defaultSeasonalDockDoorSegmentValidator(),
        clock = clock,
        journal = mutableListOf<String>(),
    )
}
