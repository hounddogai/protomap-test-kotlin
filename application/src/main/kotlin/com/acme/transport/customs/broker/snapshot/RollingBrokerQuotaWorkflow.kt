package com.acme.transport.customs.broker.snapshot

import com.acme.transport.customs.clock.Clock
import com.acme.transport.kit.flags.Registry

/** Moves rolling broker quotas through their life cycle and keeps a journal of what it did. */
data class RollingBrokerQuotaDesk(
    var store: RollingBrokerQuotaStore,
    var validator: RollingBrokerQuotaValidator,
    var clock: Clock,
    var journal: MutableList<String>,
    var engine: RollingBrokerQuotaEngine,
) {
    private fun note(message: String) {
        journal.add("${clock.now()} $message")
    }

    fun open(code: String) {
        val item = store.find(code) ?: error("rolling broker quota $code not found")
        validator.requireValid(item)
        item.moveTo(RollingBrokerQuotaStatus.GRANTED)
        store.save(item)
        note("$code is granted")
    }

    fun close(code: String, reason: String) {
        val item = store.find(code) ?: error("rolling broker quota $code not found")
        if (item.isTerminal()) {
            note("$code already closed")
            return
        }
        item.moveTo(RollingBrokerQuotaStatus.EXPIRED)
        store.save(item)
        note("$code is expired: $reason")
    }

    fun quote(code: String): Double {
        val item = store.find(code) ?: error("rolling broker quota $code not found")
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

fun newRollingBrokerQuotaDesk(store: RollingBrokerQuotaStore, clock: Clock, flags: Registry): RollingBrokerQuotaDesk {
    return RollingBrokerQuotaDesk(
        store = store,
        validator = defaultRollingBrokerQuotaValidator(),
        clock = clock,
        journal = mutableListOf<String>(),
        engine = defaultRollingBrokerQuotaEngine(flags),
    )
}
