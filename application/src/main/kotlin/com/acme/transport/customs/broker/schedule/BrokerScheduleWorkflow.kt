package com.acme.transport.customs.broker.schedule

import com.acme.transport.customs.clock.Clock

/** Moves broker schedules through their life cycle and keeps a journal of what it did. */
data class BrokerScheduleService(
    var store: BrokerScheduleStore,
    var validator: BrokerScheduleValidator,
    var clock: Clock,
    var journal: MutableList<String>,
    var engine: BrokerScheduleEngine,
) {
    private fun note(message: String) {
        journal.add("${clock.now()} $message")
    }

    fun start(code: String) {
        val item = store.find(code) ?: error("broker schedule $code not found")
        validator.requireValid(item)
        item.moveTo(BrokerScheduleStatus.GRANTED)
        store.save(item)
        note("$code is granted")
    }

    fun settle(code: String, reason: String) {
        val item = store.find(code) ?: error("broker schedule $code not found")
        if (item.isTerminal()) {
            note("$code already closed")
            return
        }
        item.moveTo(BrokerScheduleStatus.EXPIRED)
        store.save(item)
        note("$code is expired: $reason")
    }

    fun evaluate(code: String): Double {
        val item = store.find(code) ?: error("broker schedule $code not found")
        val result = engine.compute(item)
        note("$code evaluated at $result")
        return result
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

fun newBrokerScheduleService(store: BrokerScheduleStore, clock: Clock): BrokerScheduleService {
    return BrokerScheduleService(
        store = store,
        validator = defaultBrokerScheduleValidator(),
        clock = clock,
        journal = mutableListOf<String>(),
        engine = defaultBrokerScheduleEngine(),
    )
}
