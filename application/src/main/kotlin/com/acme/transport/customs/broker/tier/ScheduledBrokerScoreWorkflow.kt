package com.acme.transport.customs.broker.tier

import com.acme.transport.customs.clock.Clock

/** Moves scheduled broker scores through their life cycle and keeps a journal of what it did. */
data class ScheduledBrokerScoreCoordinator(
    var store: ScheduledBrokerScoreStore,
    var validator: ScheduledBrokerScoreValidator,
    var clock: Clock,
    var journal: MutableList<String>,
) {
    private fun note(message: String) {
        journal.add("${clock.now()} $message")
    }

    fun open(code: String) {
        val item = store.find(code) ?: error("scheduled broker score $code not found")
        validator.requireValid(item)
        item.moveTo(ScheduledBrokerScoreStatus.WARMING)
        store.save(item)
        note("$code is warming")
    }

    fun settle(code: String, reason: String) {
        val item = store.find(code) ?: error("scheduled broker score $code not found")
        if (item.isTerminal()) {
            note("$code already closed")
            return
        }
        item.moveTo(ScheduledBrokerScoreStatus.STOPPED)
        store.save(item)
        note("$code is stopped: $reason")
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

fun newScheduledBrokerScoreCoordinator(store: ScheduledBrokerScoreStore, clock: Clock): ScheduledBrokerScoreCoordinator {
    return ScheduledBrokerScoreCoordinator(
        store = store,
        validator = defaultScheduledBrokerScoreValidator(),
        clock = clock,
        journal = mutableListOf<String>(),
    )
}
