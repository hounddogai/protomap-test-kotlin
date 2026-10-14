package com.acme.transport.network.transittime.metric

import com.acme.transport.kit.flags.Registry
import com.acme.transport.network.clock.Clock

/** Moves primary transit time metrics through their life cycle and keeps a journal of what it did. */
data class PrimaryTransitTimeMetricService(
    var store: PrimaryTransitTimeMetricStore,
    var validator: PrimaryTransitTimeMetricValidator,
    var clock: Clock,
    var journal: MutableList<String>,
    var engine: PrimaryTransitTimeMetricEngine,
) {
    private fun note(message: String) {
        journal.add("${clock.now()} $message")
    }

    fun open(code: String) {
        val item = store.find(code) ?: error("primary transit time metric $code not found")
        validator.requireValid(item)
        item.moveTo(PrimaryTransitTimeMetricStatus.PENDING)
        store.save(item)
        note("$code is pending")
    }

    fun finish(code: String, reason: String) {
        val item = store.find(code) ?: error("primary transit time metric $code not found")
        if (item.isTerminal()) {
            note("$code already closed")
            return
        }
        item.moveTo(PrimaryTransitTimeMetricStatus.CLOSED)
        store.save(item)
        note("$code is closed: $reason")
    }

    fun score(code: String): Double {
        val item = store.find(code) ?: error("primary transit time metric $code not found")
        val result = engine.evaluate(item)
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

fun newPrimaryTransitTimeMetricService(store: PrimaryTransitTimeMetricStore, clock: Clock, flags: Registry): PrimaryTransitTimeMetricService {
    return PrimaryTransitTimeMetricService(
        store = store,
        validator = defaultPrimaryTransitTimeMetricValidator(),
        clock = clock,
        journal = mutableListOf<String>(),
        engine = defaultPrimaryTransitTimeMetricEngine(flags),
    )
}
