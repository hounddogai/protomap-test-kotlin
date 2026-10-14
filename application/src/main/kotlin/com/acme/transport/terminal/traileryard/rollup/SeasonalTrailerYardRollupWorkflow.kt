package com.acme.transport.terminal.traileryard.rollup

import com.acme.transport.terminal.clock.Clock

/** Moves seasonal trailer yard rollups through their life cycle and keeps a journal of what it did. */
data class SeasonalTrailerYardRollupCoordinator(
    var store: SeasonalTrailerYardRollupStore,
    var validator: SeasonalTrailerYardRollupValidator,
    var clock: Clock,
    var journal: MutableList<String>,
    var engine: SeasonalTrailerYardRollupEngine,
) {
    private fun note(message: String) {
        journal.add("${clock.now()} $message")
    }

    fun submit(code: String) {
        val item = store.find(code) ?: error("seasonal trailer yard rollup $code not found")
        validator.requireValid(item)
        item.moveTo(SeasonalTrailerYardRollupStatus.APPROVED)
        store.save(item)
        note("$code is approved")
    }

    fun settle(code: String, reason: String) {
        val item = store.find(code) ?: error("seasonal trailer yard rollup $code not found")
        if (item.isTerminal()) {
            note("$code already closed")
            return
        }
        item.moveTo(SeasonalTrailerYardRollupStatus.WITHDRAWN)
        store.save(item)
        note("$code is withdrawn: $reason")
    }

    fun score(code: String): Double {
        val item = store.find(code) ?: error("seasonal trailer yard rollup $code not found")
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

fun newSeasonalTrailerYardRollupCoordinator(store: SeasonalTrailerYardRollupStore, clock: Clock): SeasonalTrailerYardRollupCoordinator {
    return SeasonalTrailerYardRollupCoordinator(
        store = store,
        validator = defaultSeasonalTrailerYardRollupValidator(),
        clock = clock,
        journal = mutableListOf<String>(),
        engine = defaultSeasonalTrailerYardRollupEngine(),
    )
}
