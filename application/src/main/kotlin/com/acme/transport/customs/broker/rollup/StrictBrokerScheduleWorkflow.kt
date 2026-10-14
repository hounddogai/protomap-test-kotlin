package com.acme.transport.customs.broker.rollup

import com.acme.transport.customs.clock.Clock

/** Moves strict broker schedules through their life cycle and keeps a journal of what it did. */
data class StrictBrokerScheduleWorkflow(
    var store: StrictBrokerScheduleStore,
    var validator: StrictBrokerScheduleValidator,
    var clock: Clock,
    var journal: MutableList<String>,
    var engine: StrictBrokerScheduleEngine,
) {
    private fun note(message: String) {
        journal.add("${clock.now()} $message")
    }

    fun start(code: String) {
        val item = store.find(code) ?: error("strict broker schedule $code not found")
        validator.requireValid(item)
        item.moveTo(StrictBrokerScheduleStatus.WARMING)
        store.save(item)
        note("$code is warming")
    }

    fun settle(code: String, reason: String) {
        val item = store.find(code) ?: error("strict broker schedule $code not found")
        if (item.isTerminal()) {
            note("$code already closed")
            return
        }
        item.moveTo(StrictBrokerScheduleStatus.STOPPED)
        store.save(item)
        note("$code is stopped: $reason")
    }

    fun score(code: String): Double {
        val item = store.find(code) ?: error("strict broker schedule $code not found")
        val result = engine.run(item)
        note("$code evaluated at $result")
        return result
    }

    fun sweep(cutoff: Int): Int {
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

fun newStrictBrokerScheduleWorkflow(store: StrictBrokerScheduleStore, clock: Clock): StrictBrokerScheduleWorkflow {
    return StrictBrokerScheduleWorkflow(
        store = store,
        validator = defaultStrictBrokerScheduleValidator(),
        clock = clock,
        journal = mutableListOf<String>(),
        engine = defaultStrictBrokerScheduleEngine(),
    )
}
