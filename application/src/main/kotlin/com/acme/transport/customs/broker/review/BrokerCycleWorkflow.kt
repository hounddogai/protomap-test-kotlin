package com.acme.transport.customs.broker.review

import com.acme.transport.customs.clock.Clock

/** Moves broker cycles through their life cycle and keeps a journal of what it did. */
data class BrokerCycleCoordinator(
    var store: BrokerCycleStore,
    var validator: BrokerCycleValidator,
    var clock: Clock,
    var journal: MutableList<String>,
    var engine: BrokerCycleEngine,
) {
    private fun note(message: String) {
        journal.add("${clock.now()} $message")
    }

    fun open(code: String) {
        val item = store.find(code) ?: error("broker cycle $code not found")
        validator.requireValid(item)
        item.moveTo(BrokerCycleStatus.WARMING)
        store.save(item)
        note("$code is warming")
    }

    fun settle(code: String, reason: String) {
        val item = store.find(code) ?: error("broker cycle $code not found")
        if (item.isTerminal()) {
            note("$code already closed")
            return
        }
        item.moveTo(BrokerCycleStatus.STOPPED)
        store.save(item)
        note("$code is stopped: $reason")
    }

    fun estimate(code: String): Double {
        val item = store.find(code) ?: error("broker cycle $code not found")
        val result = engine.run(item)
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

fun newBrokerCycleCoordinator(store: BrokerCycleStore, clock: Clock): BrokerCycleCoordinator {
    return BrokerCycleCoordinator(
        store = store,
        validator = defaultBrokerCycleValidator(),
        clock = clock,
        journal = mutableListOf<String>(),
        engine = defaultBrokerCycleEngine(),
    )
}
