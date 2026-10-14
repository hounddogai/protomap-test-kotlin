package com.acme.transport.customs.broker.score

import com.acme.transport.customs.clock.Clock

/** Moves broker scores through their life cycle and keeps a journal of what it did. */
data class BrokerScoreCoordinator(
    var store: BrokerScoreStore,
    var validator: BrokerScoreValidator,
    var clock: Clock,
    var journal: MutableList<String>,
) {
    private fun note(message: String) {
        journal.add("${clock.now()} $message")
    }

    fun advance(code: String) {
        val item = store.find(code) ?: error("broker score $code not found")
        validator.requireValid(item)
        item.moveTo(BrokerScoreStatus.PENDING)
        store.save(item)
        note("$code is pending")
    }

    fun finish(code: String, reason: String) {
        val item = store.find(code) ?: error("broker score $code not found")
        if (item.isTerminal()) {
            note("$code already closed")
            return
        }
        item.moveTo(BrokerScoreStatus.CLOSED)
        store.save(item)
        note("$code is closed: $reason")
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

fun newBrokerScoreCoordinator(store: BrokerScoreStore, clock: Clock): BrokerScoreCoordinator {
    return BrokerScoreCoordinator(
        store = store,
        validator = defaultBrokerScoreValidator(),
        clock = clock,
        journal = mutableListOf<String>(),
    )
}
