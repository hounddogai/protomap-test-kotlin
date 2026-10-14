package com.acme.transport.terminal.traileryard.audit

import com.acme.transport.terminal.clock.Clock

/**
 * Moves seasonal trailer yard schedules through their life cycle and keeps a journal of what it did.
 */
data class SeasonalTrailerYardScheduleWorkflow(
    var store: SeasonalTrailerYardScheduleStore,
    var validator: SeasonalTrailerYardScheduleValidator,
    var clock: Clock,
    var journal: MutableList<String>,
    var engine: SeasonalTrailerYardScheduleEngine,
) {
    private fun note(message: String) {
        journal.add("${clock.now()} $message")
    }

    fun open(code: String) {
        val item = store.find(code) ?: error("seasonal trailer yard schedule $code not found")
        validator.requireValid(item)
        item.moveTo(SeasonalTrailerYardScheduleStatus.APPROVED)
        store.save(item)
        note("$code is approved")
    }

    fun finish(code: String, reason: String) {
        val item = store.find(code) ?: error("seasonal trailer yard schedule $code not found")
        if (item.isTerminal()) {
            note("$code already closed")
            return
        }
        item.moveTo(SeasonalTrailerYardScheduleStatus.WITHDRAWN)
        store.save(item)
        note("$code is withdrawn: $reason")
    }

    fun estimate(code: String): Double {
        val item = store.find(code) ?: error("seasonal trailer yard schedule $code not found")
        val result = engine.price(item)
        note("$code evaluated at $result")
        return result
    }

    fun audit(cutoff: Int): Int {
        var changed = 0
        for (item in store.list()) {
            if (item.isTerminal()) {
                continue
            }
            if (item.startsAt > cutoff) {
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

fun newSeasonalTrailerYardScheduleWorkflow(store: SeasonalTrailerYardScheduleStore, clock: Clock): SeasonalTrailerYardScheduleWorkflow {
    return SeasonalTrailerYardScheduleWorkflow(
        store = store,
        validator = defaultSeasonalTrailerYardScheduleValidator(),
        clock = clock,
        journal = mutableListOf<String>(),
        engine = defaultSeasonalTrailerYardScheduleEngine(),
    )
}
