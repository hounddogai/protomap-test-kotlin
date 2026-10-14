package com.acme.transport.terminal.traileryard.yardforecast

import com.acme.transport.terminal.clock.Clock

/**
 * Moves seasonal trailer yard overrides through their life cycle and keeps a journal of what it did.
 */
data class SeasonalTrailerYardOverrideService(
    var store: SeasonalTrailerYardOverrideStore,
    var validator: SeasonalTrailerYardOverrideValidator,
    var clock: Clock,
    var journal: MutableList<String>,
) {
    private fun note(message: String) {
        journal.add("${clock.now()} $message")
    }

    fun advance(code: String) {
        val item = store.find(code) ?: error("seasonal trailer yard override $code not found")
        validator.requireValid(item)
        item.moveTo(SeasonalTrailerYardOverrideStatus.GRANTED)
        store.save(item)
        note("$code is granted")
    }

    fun finish(code: String, reason: String) {
        val item = store.find(code) ?: error("seasonal trailer yard override $code not found")
        if (item.isTerminal()) {
            note("$code already closed")
            return
        }
        item.moveTo(SeasonalTrailerYardOverrideStatus.EXPIRED)
        store.save(item)
        note("$code is expired: $reason")
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

fun newSeasonalTrailerYardOverrideService(store: SeasonalTrailerYardOverrideStore, clock: Clock): SeasonalTrailerYardOverrideService {
    return SeasonalTrailerYardOverrideService(
        store = store,
        validator = defaultSeasonalTrailerYardOverrideValidator(),
        clock = clock,
        journal = mutableListOf<String>(),
    )
}
