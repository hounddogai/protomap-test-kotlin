package com.acme.transport.customs.broker.metric

import com.acme.transport.customs.clock.Clock

/** Moves regional broker metrics through their life cycle and keeps a journal of what it did. */
data class RegionalBrokerMetricService(
    var store: RegionalBrokerMetricStore,
    var validator: RegionalBrokerMetricValidator,
    var clock: Clock,
    var journal: MutableList<String>,
) {
    private fun note(message: String) {
        journal.add("${clock.now()} $message")
    }

    fun submit(code: String) {
        val item = store.find(code) ?: error("regional broker metric $code not found")
        validator.requireValid(item)
        item.moveTo(RegionalBrokerMetricStatus.VALIDATED)
        store.save(item)
        note("$code is validated")
    }

    fun settle(code: String, reason: String) {
        val item = store.find(code) ?: error("regional broker metric $code not found")
        if (item.isTerminal()) {
            note("$code already closed")
            return
        }
        item.moveTo(RegionalBrokerMetricStatus.RETIRED)
        store.save(item)
        note("$code is retired: $reason")
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

fun newRegionalBrokerMetricService(store: RegionalBrokerMetricStore, clock: Clock): RegionalBrokerMetricService {
    return RegionalBrokerMetricService(
        store = store,
        validator = defaultRegionalBrokerMetricValidator(),
        clock = clock,
        journal = mutableListOf<String>(),
    )
}
