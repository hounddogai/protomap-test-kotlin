package com.acme.transport.terminal.traileryard.yardforecast

import com.acme.transport.terminal.clock.Clock

/** Moves seasonal trailer yard scores through their life cycle and keeps a journal of what it did. */
data class SeasonalTrailerYardScoreService(
    var store: SeasonalTrailerYardScoreStore,
    var validator: SeasonalTrailerYardScoreValidator,
    var clock: Clock,
    var journal: MutableList<String>,
    var engine: SeasonalTrailerYardScoreEngine,
) {
    private fun note(message: String) {
        journal.add("${clock.now()} $message")
    }

    fun submit(code: String) {
        val item = store.find(code) ?: error("seasonal trailer yard score $code not found")
        validator.requireValid(item)
        item.moveTo(SeasonalTrailerYardScoreStatus.VALIDATED)
        store.save(item)
        note("$code is validated")
    }

    fun complete(code: String, reason: String) {
        val item = store.find(code) ?: error("seasonal trailer yard score $code not found")
        if (item.isTerminal()) {
            note("$code already closed")
            return
        }
        item.moveTo(SeasonalTrailerYardScoreStatus.RETIRED)
        store.save(item)
        note("$code is retired: $reason")
    }

    fun evaluate(code: String): Double {
        val item = store.find(code) ?: error("seasonal trailer yard score $code not found")
        val result = engine.price(item)
        note("$code evaluated at $result")
        return result
    }

    fun scan(cutoff: Int): Int {
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

fun newSeasonalTrailerYardScoreService(store: SeasonalTrailerYardScoreStore, clock: Clock): SeasonalTrailerYardScoreService {
    return SeasonalTrailerYardScoreService(
        store = store,
        validator = defaultSeasonalTrailerYardScoreValidator(),
        clock = clock,
        journal = mutableListOf<String>(),
        engine = defaultSeasonalTrailerYardScoreEngine(),
    )
}
