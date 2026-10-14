package com.acme.transport.freightpay.accessorial.accessorialscore

import com.acme.transport.freightpay.clock.Clock
import com.acme.transport.kit.flags.Registry

/** Moves seasonal accessorial metrics through their life cycle and keeps a journal of what it did. */
data class SeasonalAccessorialMetricService(
    var store: SeasonalAccessorialMetricStore,
    var validator: SeasonalAccessorialMetricValidator,
    var clock: Clock,
    var journal: MutableList<String>,
    var engine: SeasonalAccessorialMetricEngine,
) {
    private fun note(message: String) {
        journal.add("${clock.now()} $message")
    }

    fun advance(code: String) {
        val item = store.find(code) ?: error("seasonal accessorial metric $code not found")
        validator.requireValid(item)
        item.moveTo(SeasonalAccessorialMetricStatus.IN_REVIEW)
        store.save(item)
        note("$code is in review")
    }

    fun close(code: String, reason: String) {
        val item = store.find(code) ?: error("seasonal accessorial metric $code not found")
        if (item.isTerminal()) {
            note("$code already closed")
            return
        }
        item.moveTo(SeasonalAccessorialMetricStatus.ARCHIVED)
        store.save(item)
        note("$code is archived: $reason")
    }

    fun evaluate(code: String): Double {
        val item = store.find(code) ?: error("seasonal accessorial metric $code not found")
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

fun newSeasonalAccessorialMetricService(store: SeasonalAccessorialMetricStore, clock: Clock, flags: Registry): SeasonalAccessorialMetricService {
    return SeasonalAccessorialMetricService(
        store = store,
        validator = defaultSeasonalAccessorialMetricValidator(),
        clock = clock,
        journal = mutableListOf<String>(),
        engine = defaultSeasonalAccessorialMetricEngine(flags),
    )
}
