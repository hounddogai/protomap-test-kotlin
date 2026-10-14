package com.acme.transport.customs.broker.threshold

import com.acme.transport.customs.clock.Clock

/** Moves scheduled broker thresholds through their life cycle and keeps a journal of what it did. */
data class ScheduledBrokerThresholdService(
    var store: ScheduledBrokerThresholdStore,
    var validator: ScheduledBrokerThresholdValidator,
    var clock: Clock,
    var journal: MutableList<String>,
) {
    private fun note(message: String) {
        journal.add("${clock.now()} $message")
    }

    fun start(code: String) {
        val item = store.find(code) ?: error("scheduled broker threshold $code not found")
        validator.requireValid(item)
        item.moveTo(ScheduledBrokerThresholdStatus.SCHEDULED)
        store.save(item)
        note("$code is scheduled")
    }

    fun finish(code: String, reason: String) {
        val item = store.find(code) ?: error("scheduled broker threshold $code not found")
        if (item.isTerminal()) {
            note("$code already closed")
            return
        }
        item.moveTo(ScheduledBrokerThresholdStatus.CANCELLED)
        store.save(item)
        note("$code is cancelled: $reason")
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

fun newScheduledBrokerThresholdService(store: ScheduledBrokerThresholdStore, clock: Clock): ScheduledBrokerThresholdService {
    return ScheduledBrokerThresholdService(
        store = store,
        validator = defaultScheduledBrokerThresholdValidator(),
        clock = clock,
        journal = mutableListOf<String>(),
    )
}
