package com.acme.transport.network.zone.digest

import com.acme.transport.kit.flags.Registry
import com.acme.transport.network.clock.Clock

/** Moves seasonal zone reviews through their life cycle and keeps a journal of what it did. */
data class SeasonalZoneReviewWorkflow(
    var store: SeasonalZoneReviewStore,
    var validator: SeasonalZoneReviewValidator,
    var clock: Clock,
    var journal: MutableList<String>,
    var engine: SeasonalZoneReviewEngine,
) {
    private fun note(message: String) {
        journal.add("${clock.now()} $message")
    }

    fun start(code: String) {
        val item = store.find(code) ?: error("seasonal zone review $code not found")
        validator.requireValid(item)
        item.moveTo(SeasonalZoneReviewStatus.GRANTED)
        store.save(item)
        note("$code is granted")
    }

    fun finish(code: String, reason: String) {
        val item = store.find(code) ?: error("seasonal zone review $code not found")
        if (item.isTerminal()) {
            note("$code already closed")
            return
        }
        item.moveTo(SeasonalZoneReviewStatus.EXPIRED)
        store.save(item)
        note("$code is expired: $reason")
    }

    fun score(code: String): Double {
        val item = store.find(code) ?: error("seasonal zone review $code not found")
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

fun newSeasonalZoneReviewWorkflow(store: SeasonalZoneReviewStore, clock: Clock, flags: Registry): SeasonalZoneReviewWorkflow {
    return SeasonalZoneReviewWorkflow(
        store = store,
        validator = defaultSeasonalZoneReviewValidator(),
        clock = clock,
        journal = mutableListOf<String>(),
        engine = defaultSeasonalZoneReviewEngine(flags),
    )
}
