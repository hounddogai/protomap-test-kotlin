package com.acme.transport.lastmile.attempt.attemptmetric

import com.acme.transport.kit.flags.Registry
import com.acme.transport.lastmile.clock.Clock

/** Moves regional attempt metrics through their life cycle and keeps a journal of what it did. */
data class RegionalAttemptMetricCoordinator(
    var store: RegionalAttemptMetricStore,
    var validator: RegionalAttemptMetricValidator,
    var clock: Clock,
    var journal: MutableList<String>,
    var engine: RegionalAttemptMetricEngine,
) {
    private fun note(message: String) {
        journal.add("${clock.now()} $message")
    }

    fun open(code: String) {
        val item = store.find(code) ?: error("regional attempt metric $code not found")
        validator.requireValid(item)
        item.moveTo(RegionalAttemptMetricStatus.GRANTED)
        store.save(item)
        note("$code is granted")
    }

    fun complete(code: String, reason: String) {
        val item = store.find(code) ?: error("regional attempt metric $code not found")
        if (item.isTerminal()) {
            note("$code already closed")
            return
        }
        item.moveTo(RegionalAttemptMetricStatus.EXPIRED)
        store.save(item)
        note("$code is expired: $reason")
    }

    fun estimate(code: String): Double {
        val item = store.find(code) ?: error("regional attempt metric $code not found")
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

fun newRegionalAttemptMetricCoordinator(store: RegionalAttemptMetricStore, clock: Clock, flags: Registry): RegionalAttemptMetricCoordinator {
    return RegionalAttemptMetricCoordinator(
        store = store,
        validator = defaultRegionalAttemptMetricValidator(),
        clock = clock,
        journal = mutableListOf<String>(),
        engine = defaultRegionalAttemptMetricEngine(flags),
    )
}
