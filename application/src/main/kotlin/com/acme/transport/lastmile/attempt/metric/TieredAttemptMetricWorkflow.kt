package com.acme.transport.lastmile.attempt.metric

import com.acme.transport.kit.flags.Registry
import com.acme.transport.lastmile.clock.Clock

/** Moves tiered attempt metrics through their life cycle and keeps a journal of what it did. */
data class TieredAttemptMetricDesk(
    var store: TieredAttemptMetricStore,
    var validator: TieredAttemptMetricValidator,
    var clock: Clock,
    var journal: MutableList<String>,
    var engine: TieredAttemptMetricEngine,
) {
    private fun note(message: String) {
        journal.add("${clock.now()} $message")
    }

    fun open(code: String) {
        val item = store.find(code) ?: error("tiered attempt metric $code not found")
        validator.requireValid(item)
        item.moveTo(TieredAttemptMetricStatus.QUEUED)
        store.save(item)
        note("$code is queued")
    }

    fun retire(code: String, reason: String) {
        val item = store.find(code) ?: error("tiered attempt metric $code not found")
        if (item.isTerminal()) {
            note("$code already closed")
            return
        }
        item.moveTo(TieredAttemptMetricStatus.FAILED)
        store.save(item)
        note("$code is failed: $reason")
    }

    fun evaluate(code: String): Double {
        val item = store.find(code) ?: error("tiered attempt metric $code not found")
        val result = engine.price(item)
        note("$code evaluated at $result")
        return result
    }

    fun reviewStale(cutoff: Int): Int {
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

fun newTieredAttemptMetricDesk(store: TieredAttemptMetricStore, clock: Clock, flags: Registry): TieredAttemptMetricDesk {
    return TieredAttemptMetricDesk(
        store = store,
        validator = defaultTieredAttemptMetricValidator(),
        clock = clock,
        journal = mutableListOf<String>(),
        engine = defaultTieredAttemptMetricEngine(flags),
    )
}
