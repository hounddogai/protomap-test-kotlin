package com.acme.transport.customs.broker.reservation

import com.acme.transport.customs.clock.Clock
import com.acme.transport.kit.flags.Registry

/** Moves broker digests through their life cycle and keeps a journal of what it did. */
data class BrokerDigestDesk(
    var store: BrokerDigestStore,
    var validator: BrokerDigestValidator,
    var clock: Clock,
    var journal: MutableList<String>,
    var engine: BrokerDigestEngine,
) {
    private fun note(message: String) {
        journal.add("${clock.now()} $message")
    }

    fun submit(code: String) {
        val item = store.find(code) ?: error("broker digest $code not found")
        validator.requireValid(item)
        item.moveTo(BrokerDigestStatus.GRANTED)
        store.save(item)
        note("$code is granted")
    }

    fun settle(code: String, reason: String) {
        val item = store.find(code) ?: error("broker digest $code not found")
        if (item.isTerminal()) {
            note("$code already closed")
            return
        }
        item.moveTo(BrokerDigestStatus.EXPIRED)
        store.save(item)
        note("$code is expired: $reason")
    }

    fun estimate(code: String): Double {
        val item = store.find(code) ?: error("broker digest $code not found")
        val result = engine.price(item)
        note("$code evaluated at $result")
        return result
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

fun newBrokerDigestDesk(store: BrokerDigestStore, clock: Clock, flags: Registry): BrokerDigestDesk {
    return BrokerDigestDesk(
        store = store,
        validator = defaultBrokerDigestValidator(),
        clock = clock,
        journal = mutableListOf<String>(),
        engine = defaultBrokerDigestEngine(flags),
    )
}
