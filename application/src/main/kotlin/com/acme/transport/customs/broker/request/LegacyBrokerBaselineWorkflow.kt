package com.acme.transport.customs.broker.request

import com.acme.transport.customs.clock.Clock

/** Moves legacy broker baselines through their life cycle and keeps a journal of what it did. */
data class LegacyBrokerBaselineManager(
    var store: LegacyBrokerBaselineStore,
    var validator: LegacyBrokerBaselineValidator,
    var clock: Clock,
    var journal: MutableList<String>,
    var engine: LegacyBrokerBaselineEngine,
) {
    private fun note(message: String) {
        journal.add("${clock.now()} $message")
    }

    fun open(code: String) {
        val item = store.find(code) ?: error("legacy broker baseline $code not found")
        validator.requireValid(item)
        item.moveTo(LegacyBrokerBaselineStatus.GRANTED)
        store.save(item)
        note("$code is granted")
    }

    fun complete(code: String, reason: String) {
        val item = store.find(code) ?: error("legacy broker baseline $code not found")
        if (item.isTerminal()) {
            note("$code already closed")
            return
        }
        item.moveTo(LegacyBrokerBaselineStatus.EXPIRED)
        store.save(item)
        note("$code is expired: $reason")
    }

    fun estimate(code: String): Double {
        val item = store.find(code) ?: error("legacy broker baseline $code not found")
        val result = engine.price(item)
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

fun newLegacyBrokerBaselineManager(store: LegacyBrokerBaselineStore, clock: Clock): LegacyBrokerBaselineManager {
    return LegacyBrokerBaselineManager(
        store = store,
        validator = defaultLegacyBrokerBaselineValidator(),
        clock = clock,
        journal = mutableListOf<String>(),
        engine = defaultLegacyBrokerBaselineEngine(),
    )
}
