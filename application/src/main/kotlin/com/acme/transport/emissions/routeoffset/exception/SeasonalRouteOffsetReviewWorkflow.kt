package com.acme.transport.emissions.routeoffset.exception

import com.acme.transport.emissions.clock.Clock
import com.acme.transport.kit.flags.Registry

/** Moves seasonal route offset reviews through their life cycle and keeps a journal of what it did. */
data class SeasonalRouteOffsetReviewWorkflow(
    var store: SeasonalRouteOffsetReviewStore,
    var validator: SeasonalRouteOffsetReviewValidator,
    var clock: Clock,
    var journal: MutableList<String>,
    var engine: SeasonalRouteOffsetReviewEngine,
) {
    private fun note(message: String) {
        journal.add("${clock.now()} $message")
    }

    fun submit(code: String) {
        val item = store.find(code) ?: error("seasonal route offset review $code not found")
        validator.requireValid(item)
        item.moveTo(SeasonalRouteOffsetReviewStatus.VALIDATED)
        store.save(item)
        note("$code is validated")
    }

    fun settle(code: String, reason: String) {
        val item = store.find(code) ?: error("seasonal route offset review $code not found")
        if (item.isTerminal()) {
            note("$code already closed")
            return
        }
        item.moveTo(SeasonalRouteOffsetReviewStatus.RETIRED)
        store.save(item)
        note("$code is retired: $reason")
    }

    fun estimate(code: String): Double {
        val item = store.find(code) ?: error("seasonal route offset review $code not found")
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
            if (item.createdAt > cutoff) {
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

fun newSeasonalRouteOffsetReviewWorkflow(store: SeasonalRouteOffsetReviewStore, clock: Clock, flags: Registry): SeasonalRouteOffsetReviewWorkflow {
    return SeasonalRouteOffsetReviewWorkflow(
        store = store,
        validator = defaultSeasonalRouteOffsetReviewValidator(),
        clock = clock,
        journal = mutableListOf<String>(),
        engine = defaultSeasonalRouteOffsetReviewEngine(flags),
    )
}
