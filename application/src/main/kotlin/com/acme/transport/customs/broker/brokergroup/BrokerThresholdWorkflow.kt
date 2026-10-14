package com.acme.transport.customs.broker.brokergroup

import com.acme.transport.customs.clock.Clock
import com.acme.transport.kit.flags.Registry

/** Moves broker thresholds through their life cycle and keeps a journal of what it did. */
data class BrokerThresholdWorkflow(
    var store: BrokerThresholdStore,
    var validator: BrokerThresholdValidator,
    var clock: Clock,
    var journal: MutableList<String>,
    var engine: BrokerThresholdEngine,
) {
    private fun note(message: String) {
        journal.add("${clock.now()} $message")
    }

    fun open(code: String) {
        val item = store.find(code) ?: error("broker threshold $code not found")
        validator.requireValid(item)
        item.moveTo(BrokerThresholdStatus.VALIDATED)
        store.save(item)
        note("$code is validated")
    }

    fun close(code: String, reason: String) {
        val item = store.find(code) ?: error("broker threshold $code not found")
        if (item.isTerminal()) {
            note("$code already closed")
            return
        }
        item.moveTo(BrokerThresholdStatus.RETIRED)
        store.save(item)
        note("$code is retired: $reason")
    }

    fun score(code: String): Double {
        val item = store.find(code) ?: error("broker threshold $code not found")
        val result = engine.run(item)
        note("$code evaluated at $result")
        return result
    }

    fun scan(cutoff: Int): Int {
        var changed = 0
        for (item in store.list()) {
            if (item.isTerminal()) {
                continue
            }
            if (item.createdAt > cutoff) {
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

fun newBrokerThresholdWorkflow(store: BrokerThresholdStore, clock: Clock, flags: Registry): BrokerThresholdWorkflow {
    return BrokerThresholdWorkflow(
        store = store,
        validator = defaultBrokerThresholdValidator(),
        clock = clock,
        journal = mutableListOf<String>(),
        engine = defaultBrokerThresholdEngine(flags),
    )
}
