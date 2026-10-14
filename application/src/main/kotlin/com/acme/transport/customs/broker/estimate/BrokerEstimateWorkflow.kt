package com.acme.transport.customs.broker.estimate

import com.acme.transport.customs.clock.Clock

/** Moves broker estimates through their life cycle and keeps a journal of what it did. */
data class BrokerEstimateWorkflow(
    var store: BrokerEstimateStore,
    var validator: BrokerEstimateValidator,
    var clock: Clock,
    var journal: MutableList<String>,
) {
    private fun note(message: String) {
        journal.add("${clock.now()} $message")
    }

    fun open(code: String) {
        val item = store.find(code) ?: error("broker estimate $code not found")
        validator.requireValid(item)
        item.moveTo(BrokerEstimateStatus.APPROVED)
        store.save(item)
        note("$code is approved")
    }

    fun complete(code: String, reason: String) {
        val item = store.find(code) ?: error("broker estimate $code not found")
        if (item.isTerminal()) {
            note("$code already closed")
            return
        }
        item.moveTo(BrokerEstimateStatus.WITHDRAWN)
        store.save(item)
        note("$code is withdrawn: $reason")
    }

    fun reviewStale(cutoff: Int): Int {
        var changed = 0
        for (item in store.list()) {
            if (item.isTerminal()) {
                continue
            }
            if (item.startsAt > cutoff) {
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

fun newBrokerEstimateWorkflow(store: BrokerEstimateStore, clock: Clock): BrokerEstimateWorkflow {
    return BrokerEstimateWorkflow(
        store = store,
        validator = defaultBrokerEstimateValidator(),
        clock = clock,
        journal = mutableListOf<String>(),
    )
}
