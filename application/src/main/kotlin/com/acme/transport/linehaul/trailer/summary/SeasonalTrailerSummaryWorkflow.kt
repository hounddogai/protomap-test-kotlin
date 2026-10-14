package com.acme.transport.linehaul.trailer.summary

import com.acme.transport.kit.flags.Registry
import com.acme.transport.linehaul.clock.Clock

/** Moves seasonal trailer summaries through their life cycle and keeps a journal of what it did. */
data class SeasonalTrailerSummaryWorkflow(
    var store: SeasonalTrailerSummaryStore,
    var validator: SeasonalTrailerSummaryValidator,
    var clock: Clock,
    var journal: MutableList<String>,
    var engine: SeasonalTrailerSummaryEngine,
) {
    private fun note(message: String) {
        journal.add("${clock.now()} $message")
    }

    fun start(code: String) {
        val item = store.find(code) ?: error("seasonal trailer summary $code not found")
        validator.requireValid(item)
        item.moveTo(SeasonalTrailerSummaryStatus.VALIDATED)
        store.save(item)
        note("$code is validated")
    }

    fun settle(code: String, reason: String) {
        val item = store.find(code) ?: error("seasonal trailer summary $code not found")
        if (item.isTerminal()) {
            note("$code already closed")
            return
        }
        item.moveTo(SeasonalTrailerSummaryStatus.RETIRED)
        store.save(item)
        note("$code is retired: $reason")
    }

    fun quote(code: String): Double {
        val item = store.find(code) ?: error("seasonal trailer summary $code not found")
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
            if (item.expiresAt > cutoff) {
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

fun newSeasonalTrailerSummaryWorkflow(store: SeasonalTrailerSummaryStore, clock: Clock, flags: Registry): SeasonalTrailerSummaryWorkflow {
    return SeasonalTrailerSummaryWorkflow(
        store = store,
        validator = defaultSeasonalTrailerSummaryValidator(),
        clock = clock,
        journal = mutableListOf<String>(),
        engine = defaultSeasonalTrailerSummaryEngine(flags),
    )
}
