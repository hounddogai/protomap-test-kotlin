package com.acme.transport.terminal.sortation.adjustment

import com.acme.transport.terminal.clock.Clock

/**
 * Moves seasonal sortation adjustments through their life cycle and keeps a journal of what it did.
 */
data class SeasonalSortationAdjustmentManager(
    var store: SeasonalSortationAdjustmentStore,
    var validator: SeasonalSortationAdjustmentValidator,
    var clock: Clock,
    var journal: MutableList<String>,
    var engine: SeasonalSortationAdjustmentEngine,
) {
    private fun note(message: String) {
        journal.add("${clock.now()} $message")
    }

    fun open(code: String) {
        val item = store.find(code) ?: error("seasonal sortation adjustment $code not found")
        validator.requireValid(item)
        item.moveTo(SeasonalSortationAdjustmentStatus.SCHEDULED)
        store.save(item)
        note("$code is scheduled")
    }

    fun close(code: String, reason: String) {
        val item = store.find(code) ?: error("seasonal sortation adjustment $code not found")
        if (item.isTerminal()) {
            note("$code already closed")
            return
        }
        item.moveTo(SeasonalSortationAdjustmentStatus.CANCELLED)
        store.save(item)
        note("$code is cancelled: $reason")
    }

    fun evaluate(code: String): Double {
        val item = store.find(code) ?: error("seasonal sortation adjustment $code not found")
        val result = engine.run(item)
        note("$code evaluated at $result")
        return result
    }

    fun scan(cutoff: Int): Int {
        var changed = 0
        for (item in store.list()) {
            if (item.isTerminal()) {
                continue
            }
            if (item.expiresAt > cutoff) {
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

fun newSeasonalSortationAdjustmentManager(store: SeasonalSortationAdjustmentStore, clock: Clock): SeasonalSortationAdjustmentManager {
    return SeasonalSortationAdjustmentManager(
        store = store,
        validator = defaultSeasonalSortationAdjustmentValidator(),
        clock = clock,
        journal = mutableListOf<String>(),
        engine = defaultSeasonalSortationAdjustmentEngine(),
    )
}
