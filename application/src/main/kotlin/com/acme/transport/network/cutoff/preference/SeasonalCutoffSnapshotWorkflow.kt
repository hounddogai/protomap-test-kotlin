package com.acme.transport.network.cutoff.preference

import com.acme.transport.network.clock.Clock

/** Moves seasonal cutoff snapshots through their life cycle and keeps a journal of what it did. */
data class SeasonalCutoffSnapshotManager(
    var store: SeasonalCutoffSnapshotStore,
    var validator: SeasonalCutoffSnapshotValidator,
    var clock: Clock,
    var journal: MutableList<String>,
    var engine: SeasonalCutoffSnapshotEngine,
) {
    private fun note(message: String) {
        journal.add("${clock.now()} $message")
    }

    fun open(code: String) {
        val item = store.find(code) ?: error("seasonal cutoff snapshot $code not found")
        validator.requireValid(item)
        item.moveTo(SeasonalCutoffSnapshotStatus.QUEUED)
        store.save(item)
        note("$code is queued")
    }

    fun settle(code: String, reason: String) {
        val item = store.find(code) ?: error("seasonal cutoff snapshot $code not found")
        if (item.isTerminal()) {
            note("$code already closed")
            return
        }
        item.moveTo(SeasonalCutoffSnapshotStatus.FAILED)
        store.save(item)
        note("$code is failed: $reason")
    }

    fun estimate(code: String): Double {
        val item = store.find(code) ?: error("seasonal cutoff snapshot $code not found")
        val result = engine.price(item)
        note("$code evaluated at $result")
        return result
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

fun newSeasonalCutoffSnapshotManager(store: SeasonalCutoffSnapshotStore, clock: Clock): SeasonalCutoffSnapshotManager {
    return SeasonalCutoffSnapshotManager(
        store = store,
        validator = defaultSeasonalCutoffSnapshotValidator(),
        clock = clock,
        journal = mutableListOf<String>(),
        engine = defaultSeasonalCutoffSnapshotEngine(),
    )
}
