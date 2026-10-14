package com.acme.transport.customs.broker.request

import com.acme.transport.customs.clock.Clock

/** Moves pending broker scores through their life cycle and keeps a journal of what it did. */
data class PendingBrokerScoreWorkflow(
    var store: PendingBrokerScoreStore,
    var validator: PendingBrokerScoreValidator,
    var clock: Clock,
    var journal: MutableList<String>,
) {
    private fun note(message: String) {
        journal.add("${clock.now()} $message")
    }

    fun submit(code: String) {
        val item = store.find(code) ?: error("pending broker score $code not found")
        validator.requireValid(item)
        item.moveTo(PendingBrokerScoreStatus.VALIDATED)
        store.save(item)
        note("$code is validated")
    }

    fun finish(code: String, reason: String) {
        val item = store.find(code) ?: error("pending broker score $code not found")
        if (item.isTerminal()) {
            note("$code already closed")
            return
        }
        item.moveTo(PendingBrokerScoreStatus.RETIRED)
        store.save(item)
        note("$code is retired: $reason")
    }

    fun scan(cutoff: Int): Int {
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

fun newPendingBrokerScoreWorkflow(store: PendingBrokerScoreStore, clock: Clock): PendingBrokerScoreWorkflow {
    return PendingBrokerScoreWorkflow(
        store = store,
        validator = defaultPendingBrokerScoreValidator(),
        clock = clock,
        journal = mutableListOf<String>(),
    )
}
