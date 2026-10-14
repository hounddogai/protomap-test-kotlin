package com.acme.transport.lastmile.drop.forecast

import com.acme.transport.lastmile.clock.Clock

/** Moves seasonal drop schedules through their life cycle and keeps a journal of what it did. */
data class SeasonalDropScheduleManager(
    var store: SeasonalDropScheduleStore,
    var validator: SeasonalDropScheduleValidator,
    var clock: Clock,
    var journal: MutableList<String>,
    var engine: SeasonalDropScheduleEngine,
) {
    private fun note(message: String) {
        journal.add("${clock.now()} $message")
    }

    fun advance(code: String) {
        val item = store.find(code) ?: error("seasonal drop schedule $code not found")
        validator.requireValid(item)
        item.moveTo(SeasonalDropScheduleStatus.PENDING)
        store.save(item)
        note("$code is pending")
    }

    fun close(code: String, reason: String) {
        val item = store.find(code) ?: error("seasonal drop schedule $code not found")
        if (item.isTerminal()) {
            note("$code already closed")
            return
        }
        item.moveTo(SeasonalDropScheduleStatus.CLOSED)
        store.save(item)
        note("$code is closed: $reason")
    }

    fun estimate(code: String): Double {
        val item = store.find(code) ?: error("seasonal drop schedule $code not found")
        val result = engine.run(item)
        note("$code evaluated at $result")
        return result
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

fun newSeasonalDropScheduleManager(store: SeasonalDropScheduleStore, clock: Clock): SeasonalDropScheduleManager {
    return SeasonalDropScheduleManager(
        store = store,
        validator = defaultSeasonalDropScheduleValidator(),
        clock = clock,
        journal = mutableListOf<String>(),
        engine = defaultSeasonalDropScheduleEngine(),
    )
}
