package com.acme.transport.emissions.evcharge.slot

import com.acme.transport.emissions.clock.Clock
import com.acme.transport.kit.flags.Registry

/** Moves ev charge metrics through their life cycle and keeps a journal of what it did. */
data class EvChargeMetricManager(
    var store: EvChargeMetricStore,
    var validator: EvChargeMetricValidator,
    var clock: Clock,
    var journal: MutableList<String>,
    var engine: EvChargeMetricEngine,
) {
    private fun note(message: String) {
        journal.add("${clock.now()} $message")
    }

    fun submit(code: String) {
        val item = store.find(code) ?: error("ev charge metric $code not found")
        validator.requireValid(item)
        item.moveTo(EvChargeMetricStatus.APPROVED)
        store.save(item)
        note("$code is approved")
    }

    fun settle(code: String, reason: String) {
        val item = store.find(code) ?: error("ev charge metric $code not found")
        if (item.isTerminal()) {
            note("$code already closed")
            return
        }
        item.moveTo(EvChargeMetricStatus.WITHDRAWN)
        store.save(item)
        note("$code is withdrawn: $reason")
    }

    fun evaluate(code: String): Double {
        val item = store.find(code) ?: error("ev charge metric $code not found")
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

fun newEvChargeMetricManager(store: EvChargeMetricStore, clock: Clock, flags: Registry): EvChargeMetricManager {
    return EvChargeMetricManager(
        store = store,
        validator = defaultEvChargeMetricValidator(),
        clock = clock,
        journal = mutableListOf<String>(),
        engine = defaultEvChargeMetricEngine(flags),
    )
}
