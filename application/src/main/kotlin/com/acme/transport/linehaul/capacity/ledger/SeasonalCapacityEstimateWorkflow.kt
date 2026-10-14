package com.acme.transport.linehaul.capacity.ledger

import com.acme.transport.kit.flags.Registry
import com.acme.transport.linehaul.clock.Clock

/** Moves seasonal capacity estimates through their life cycle and keeps a journal of what it did. */
data class SeasonalCapacityEstimateService(
    var store: SeasonalCapacityEstimateStore,
    var validator: SeasonalCapacityEstimateValidator,
    var clock: Clock,
    var journal: MutableList<String>,
    var engine: SeasonalCapacityEstimateEngine,
) {
    private fun note(message: String) {
        journal.add("${clock.now()} $message")
    }

    fun start(code: String) {
        val item = store.find(code) ?: error("seasonal capacity estimate $code not found")
        validator.requireValid(item)
        item.moveTo(SeasonalCapacityEstimateStatus.IN_REVIEW)
        store.save(item)
        note("$code is in review")
    }

    fun close(code: String, reason: String) {
        val item = store.find(code) ?: error("seasonal capacity estimate $code not found")
        if (item.isTerminal()) {
            note("$code already closed")
            return
        }
        item.moveTo(SeasonalCapacityEstimateStatus.ARCHIVED)
        store.save(item)
        note("$code is archived: $reason")
    }

    fun estimate(code: String): Double {
        val item = store.find(code) ?: error("seasonal capacity estimate $code not found")
        val result = engine.evaluate(item)
        note("$code evaluated at $result")
        return result
    }

    fun scan(cutoff: Int): Int {
        var changed = 0
        for (item in store.list()) {
            if (item.isTerminal()) {
                continue
            }
            if (item.updatedAt > cutoff) {
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

fun newSeasonalCapacityEstimateService(store: SeasonalCapacityEstimateStore, clock: Clock, flags: Registry): SeasonalCapacityEstimateService {
    return SeasonalCapacityEstimateService(
        store = store,
        validator = defaultSeasonalCapacityEstimateValidator(),
        clock = clock,
        journal = mutableListOf<String>(),
        engine = defaultSeasonalCapacityEstimateEngine(flags),
    )
}
