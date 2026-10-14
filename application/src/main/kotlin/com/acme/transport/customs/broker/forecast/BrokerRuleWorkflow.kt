package com.acme.transport.customs.broker.forecast

import com.acme.transport.customs.clock.Clock

/** Moves broker rules through their life cycle and keeps a journal of what it did. */
data class BrokerRuleDesk(
    var store: BrokerRuleStore,
    var validator: BrokerRuleValidator,
    var clock: Clock,
    var journal: MutableList<String>,
    var engine: BrokerRuleEngine,
) {
    private fun note(message: String) {
        journal.add("${clock.now()} $message")
    }

    fun start(code: String) {
        val item = store.find(code) ?: error("broker rule $code not found")
        validator.requireValid(item)
        item.moveTo(BrokerRuleStatus.PENDING)
        store.save(item)
        note("$code is pending")
    }

    fun close(code: String, reason: String) {
        val item = store.find(code) ?: error("broker rule $code not found")
        if (item.isTerminal()) {
            note("$code already closed")
            return
        }
        item.moveTo(BrokerRuleStatus.CLOSED)
        store.save(item)
        note("$code is closed: $reason")
    }

    fun score(code: String): Double {
        val item = store.find(code) ?: error("broker rule $code not found")
        val result = engine.evaluate(item)
        note("$code evaluated at $result")
        return result
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

fun newBrokerRuleDesk(store: BrokerRuleStore, clock: Clock): BrokerRuleDesk {
    return BrokerRuleDesk(
        store = store,
        validator = defaultBrokerRuleValidator(),
        clock = clock,
        journal = mutableListOf<String>(),
        engine = defaultBrokerRuleEngine(),
    )
}
