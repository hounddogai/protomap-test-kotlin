package com.acme.transport.terminal.traileryard.yardreservation

import com.acme.transport.terminal.clock.Clock

/**
 * Moves regional trailer yard summaries through their life cycle and keeps a journal of what it did.
 */
data class RegionalTrailerYardSummaryCoordinator(
    var store: RegionalTrailerYardSummaryStore,
    var validator: RegionalTrailerYardSummaryValidator,
    var clock: Clock,
    var journal: MutableList<String>,
    var engine: RegionalTrailerYardSummaryEngine,
) {
    private fun note(message: String) {
        journal.add("${clock.now()} $message")
    }

    fun advance(code: String) {
        val item = store.find(code) ?: error("regional trailer yard summary $code not found")
        validator.requireValid(item)
        item.moveTo(RegionalTrailerYardSummaryStatus.WARMING)
        store.save(item)
        note("$code is warming")
    }

    fun complete(code: String, reason: String) {
        val item = store.find(code) ?: error("regional trailer yard summary $code not found")
        if (item.isTerminal()) {
            note("$code already closed")
            return
        }
        item.moveTo(RegionalTrailerYardSummaryStatus.STOPPED)
        store.save(item)
        note("$code is stopped: $reason")
    }

    fun estimate(code: String): Double {
        val item = store.find(code) ?: error("regional trailer yard summary $code not found")
        val result = engine.evaluate(item)
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

fun newRegionalTrailerYardSummaryCoordinator(store: RegionalTrailerYardSummaryStore, clock: Clock): RegionalTrailerYardSummaryCoordinator {
    return RegionalTrailerYardSummaryCoordinator(
        store = store,
        validator = defaultRegionalTrailerYardSummaryValidator(),
        clock = clock,
        journal = mutableListOf<String>(),
        engine = defaultRegionalTrailerYardSummaryEngine(),
    )
}
