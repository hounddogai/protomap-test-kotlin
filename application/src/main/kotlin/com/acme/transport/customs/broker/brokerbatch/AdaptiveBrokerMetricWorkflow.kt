package com.acme.transport.customs.broker.brokerbatch

import com.acme.transport.customs.clock.Clock

/** Moves adaptive broker metrics through their life cycle and keeps a journal of what it did. */
data class AdaptiveBrokerMetricCoordinator(
    var store: AdaptiveBrokerMetricStore,
    var validator: AdaptiveBrokerMetricValidator,
    var clock: Clock,
    var journal: MutableList<String>,
) {
    private fun note(message: String) {
        journal.add("${clock.now()} $message")
    }

    fun start(code: String) {
        val item = store.find(code) ?: error("adaptive broker metric $code not found")
        validator.requireValid(item)
        item.moveTo(AdaptiveBrokerMetricStatus.GRANTED)
        store.save(item)
        note("$code is granted")
    }

    fun finish(code: String, reason: String) {
        val item = store.find(code) ?: error("adaptive broker metric $code not found")
        if (item.isTerminal()) {
            note("$code already closed")
            return
        }
        item.moveTo(AdaptiveBrokerMetricStatus.EXPIRED)
        store.save(item)
        note("$code is expired: $reason")
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

fun newAdaptiveBrokerMetricCoordinator(store: AdaptiveBrokerMetricStore, clock: Clock): AdaptiveBrokerMetricCoordinator {
    return AdaptiveBrokerMetricCoordinator(
        store = store,
        validator = defaultAdaptiveBrokerMetricValidator(),
        clock = clock,
        journal = mutableListOf<String>(),
    )
}
