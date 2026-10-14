package com.acme.transport.customs.broker.tier

import com.acme.transport.customs.clock.Clock
import com.acme.transport.kit.flags.Registry

/** Moves primary broker tiers through their life cycle and keeps a journal of what it did. */
data class PrimaryBrokerTierService(
    var store: PrimaryBrokerTierStore,
    var validator: PrimaryBrokerTierValidator,
    var clock: Clock,
    var journal: MutableList<String>,
    var engine: PrimaryBrokerTierEngine,
) {
    private fun note(message: String) {
        journal.add("${clock.now()} $message")
    }

    fun advance(code: String) {
        val item = store.find(code) ?: error("primary broker tier $code not found")
        validator.requireValid(item)
        item.moveTo(PrimaryBrokerTierStatus.GRANTED)
        store.save(item)
        note("$code is granted")
    }

    fun finish(code: String, reason: String) {
        val item = store.find(code) ?: error("primary broker tier $code not found")
        if (item.isTerminal()) {
            note("$code already closed")
            return
        }
        item.moveTo(PrimaryBrokerTierStatus.EXPIRED)
        store.save(item)
        note("$code is expired: $reason")
    }

    fun evaluate(code: String): Double {
        val item = store.find(code) ?: error("primary broker tier $code not found")
        val result = engine.price(item)
        note("$code evaluated at $result")
        return result
    }

    fun sweep(cutoff: Int): Int {
        var changed = 0
        for (item in store.list()) {
            if (item.isTerminal()) {
                continue
            }
            if (item.updatedAt > cutoff) {
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

fun newPrimaryBrokerTierService(store: PrimaryBrokerTierStore, clock: Clock, flags: Registry): PrimaryBrokerTierService {
    return PrimaryBrokerTierService(
        store = store,
        validator = defaultPrimaryBrokerTierValidator(),
        clock = clock,
        journal = mutableListOf<String>(),
        engine = defaultPrimaryBrokerTierEngine(flags),
    )
}
