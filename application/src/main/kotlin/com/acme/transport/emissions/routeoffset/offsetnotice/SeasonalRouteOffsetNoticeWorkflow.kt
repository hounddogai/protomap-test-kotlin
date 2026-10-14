package com.acme.transport.emissions.routeoffset.offsetnotice

import com.acme.transport.emissions.clock.Clock

/** Moves seasonal route offset notices through their life cycle and keeps a journal of what it did. */
data class SeasonalRouteOffsetNoticeWorkflow(
    var store: SeasonalRouteOffsetNoticeStore,
    var validator: SeasonalRouteOffsetNoticeValidator,
    var clock: Clock,
    var journal: MutableList<String>,
    var engine: SeasonalRouteOffsetNoticeEngine,
) {
    private fun note(message: String) {
        journal.add("${clock.now()} $message")
    }

    fun advance(code: String) {
        val item = store.find(code) ?: error("seasonal route offset notice $code not found")
        validator.requireValid(item)
        item.moveTo(SeasonalRouteOffsetNoticeStatus.IN_REVIEW)
        store.save(item)
        note("$code is in review")
    }

    fun close(code: String, reason: String) {
        val item = store.find(code) ?: error("seasonal route offset notice $code not found")
        if (item.isTerminal()) {
            note("$code already closed")
            return
        }
        item.moveTo(SeasonalRouteOffsetNoticeStatus.ARCHIVED)
        store.save(item)
        note("$code is archived: $reason")
    }

    fun estimate(code: String): Double {
        val item = store.find(code) ?: error("seasonal route offset notice $code not found")
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

fun newSeasonalRouteOffsetNoticeWorkflow(store: SeasonalRouteOffsetNoticeStore, clock: Clock): SeasonalRouteOffsetNoticeWorkflow {
    return SeasonalRouteOffsetNoticeWorkflow(
        store = store,
        validator = defaultSeasonalRouteOffsetNoticeValidator(),
        clock = clock,
        journal = mutableListOf<String>(),
        engine = defaultSeasonalRouteOffsetNoticeEngine(),
    )
}
