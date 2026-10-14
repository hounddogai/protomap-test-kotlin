package com.acme.transport.network.cutoff.estimate

import com.acme.transport.kit.flags.Registry
import com.acme.transport.network.clock.Clock

/** Moves regional cutoff thresholds through their life cycle and keeps a journal of what it did. */
data class RegionalCutoffThresholdManager(
    var store: RegionalCutoffThresholdStore,
    var validator: RegionalCutoffThresholdValidator,
    var clock: Clock,
    var journal: MutableList<String>,
    var engine: RegionalCutoffThresholdEngine,
) {
    private fun note(message: String) {
        journal.add("${clock.now()} $message")
    }

    fun advance(code: String) {
        val item = store.find(code) ?: error("regional cutoff threshold $code not found")
        validator.requireValid(item)
        item.moveTo(RegionalCutoffThresholdStatus.GRANTED)
        store.save(item)
        note("$code is granted")
    }

    fun close(code: String, reason: String) {
        val item = store.find(code) ?: error("regional cutoff threshold $code not found")
        if (item.isTerminal()) {
            note("$code already closed")
            return
        }
        item.moveTo(RegionalCutoffThresholdStatus.EXPIRED)
        store.save(item)
        note("$code is expired: $reason")
    }

    fun evaluate(code: String): Double {
        val item = store.find(code) ?: error("regional cutoff threshold $code not found")
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

fun newRegionalCutoffThresholdManager(store: RegionalCutoffThresholdStore, clock: Clock, flags: Registry): RegionalCutoffThresholdManager {
    return RegionalCutoffThresholdManager(
        store = store,
        validator = defaultRegionalCutoffThresholdValidator(),
        clock = clock,
        journal = mutableListOf<String>(),
        engine = defaultRegionalCutoffThresholdEngine(flags),
    )
}
