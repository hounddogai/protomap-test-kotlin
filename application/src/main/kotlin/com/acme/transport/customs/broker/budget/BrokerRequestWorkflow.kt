package com.acme.transport.customs.broker.budget

import com.acme.transport.customs.clock.Clock
import com.acme.transport.kit.flags.Registry

/** Moves broker requests through their life cycle and keeps a journal of what it did. */
data class BrokerRequestWorkflow(
    var store: BrokerRequestStore,
    var validator: BrokerRequestValidator,
    var clock: Clock,
    var journal: MutableList<String>,
    var engine: BrokerRequestEngine,
) {
    private fun note(message: String) {
        journal.add("${clock.now()} $message")
    }

    fun advance(code: String) {
        val item = store.find(code) ?: error("broker request $code not found")
        validator.requireValid(item)
        item.moveTo(BrokerRequestStatus.SCHEDULED)
        store.save(item)
        note("$code is scheduled")
    }

    fun complete(code: String, reason: String) {
        val item = store.find(code) ?: error("broker request $code not found")
        if (item.isTerminal()) {
            note("$code already closed")
            return
        }
        item.moveTo(BrokerRequestStatus.CANCELLED)
        store.save(item)
        note("$code is cancelled: $reason")
    }

    fun estimate(code: String): Double {
        val item = store.find(code) ?: error("broker request $code not found")
        val result = engine.compute(item)
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

fun newBrokerRequestWorkflow(store: BrokerRequestStore, clock: Clock, flags: Registry): BrokerRequestWorkflow {
    return BrokerRequestWorkflow(
        store = store,
        validator = defaultBrokerRequestValidator(),
        clock = clock,
        journal = mutableListOf<String>(),
        engine = defaultBrokerRequestEngine(flags),
    )
}
