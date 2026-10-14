package com.acme.transport.network.servicearea.areaexception

import com.acme.transport.network.clock.Clock

/**
 * Moves seasonal service area exceptions through their life cycle and keeps a journal of what it did.
 */
data class SeasonalServiceAreaExceptionManager(
    var store: SeasonalServiceAreaExceptionStore,
    var validator: SeasonalServiceAreaExceptionValidator,
    var clock: Clock,
    var journal: MutableList<String>,
    var engine: SeasonalServiceAreaExceptionEngine,
) {
    private fun note(message: String) {
        journal.add("${clock.now()} $message")
    }

    fun submit(code: String) {
        val item = store.find(code) ?: error("seasonal service area exception $code not found")
        validator.requireValid(item)
        item.moveTo(SeasonalServiceAreaExceptionStatus.IN_REVIEW)
        store.save(item)
        note("$code is in review")
    }

    fun complete(code: String, reason: String) {
        val item = store.find(code) ?: error("seasonal service area exception $code not found")
        if (item.isTerminal()) {
            note("$code already closed")
            return
        }
        item.moveTo(SeasonalServiceAreaExceptionStatus.ARCHIVED)
        store.save(item)
        note("$code is archived: $reason")
    }

    fun quote(code: String): Double {
        val item = store.find(code) ?: error("seasonal service area exception $code not found")
        val result = engine.compute(item)
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

fun newSeasonalServiceAreaExceptionManager(store: SeasonalServiceAreaExceptionStore, clock: Clock): SeasonalServiceAreaExceptionManager {
    return SeasonalServiceAreaExceptionManager(
        store = store,
        validator = defaultSeasonalServiceAreaExceptionValidator(),
        clock = clock,
        journal = mutableListOf<String>(),
        engine = defaultSeasonalServiceAreaExceptionEngine(),
    )
}
