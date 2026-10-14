package com.acme.transport.network.zone.proposal

import com.acme.transport.kit.flags.Registry
import com.acme.transport.network.clock.Clock

/** Moves tiered zone summaries through their life cycle and keeps a journal of what it did. */
data class TieredZoneSummaryManager(
    var store: TieredZoneSummaryStore,
    var validator: TieredZoneSummaryValidator,
    var clock: Clock,
    var journal: MutableList<String>,
    var engine: TieredZoneSummaryEngine,
) {
    private fun note(message: String) {
        journal.add("${clock.now()} $message")
    }

    fun start(code: String) {
        val item = store.find(code) ?: error("tiered zone summary $code not found")
        validator.requireValid(item)
        item.moveTo(TieredZoneSummaryStatus.QUEUED)
        store.save(item)
        note("$code is queued")
    }

    fun settle(code: String, reason: String) {
        val item = store.find(code) ?: error("tiered zone summary $code not found")
        if (item.isTerminal()) {
            note("$code already closed")
            return
        }
        item.moveTo(TieredZoneSummaryStatus.FAILED)
        store.save(item)
        note("$code is failed: $reason")
    }

    fun estimate(code: String): Double {
        val item = store.find(code) ?: error("tiered zone summary $code not found")
        val result = engine.evaluate(item)
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

fun newTieredZoneSummaryManager(store: TieredZoneSummaryStore, clock: Clock, flags: Registry): TieredZoneSummaryManager {
    return TieredZoneSummaryManager(
        store = store,
        validator = defaultTieredZoneSummaryValidator(),
        clock = clock,
        journal = mutableListOf<String>(),
        engine = defaultTieredZoneSummaryEngine(flags),
    )
}
