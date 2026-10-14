package com.acme.transport.customs.broker.group

import com.acme.transport.customs.clock.Clock
import com.acme.transport.kit.flags.Registry

/** Moves default broker overrides through their life cycle and keeps a journal of what it did. */
data class DefaultBrokerOverrideCoordinator(
    var store: DefaultBrokerOverrideStore,
    var validator: DefaultBrokerOverrideValidator,
    var clock: Clock,
    var journal: MutableList<String>,
    var engine: DefaultBrokerOverrideEngine,
) {
    private fun note(message: String) {
        journal.add("${clock.now()} $message")
    }

    fun advance(code: String) {
        val item = store.find(code) ?: error("default broker override $code not found")
        validator.requireValid(item)
        item.moveTo(DefaultBrokerOverrideStatus.QUEUED)
        store.save(item)
        note("$code is queued")
    }

    fun retire(code: String, reason: String) {
        val item = store.find(code) ?: error("default broker override $code not found")
        if (item.isTerminal()) {
            note("$code already closed")
            return
        }
        item.moveTo(DefaultBrokerOverrideStatus.FAILED)
        store.save(item)
        note("$code is failed: $reason")
    }

    fun evaluate(code: String): Double {
        val item = store.find(code) ?: error("default broker override $code not found")
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

fun newDefaultBrokerOverrideCoordinator(store: DefaultBrokerOverrideStore, clock: Clock, flags: Registry): DefaultBrokerOverrideCoordinator {
    return DefaultBrokerOverrideCoordinator(
        store = store,
        validator = defaultDefaultBrokerOverrideValidator(),
        clock = clock,
        journal = mutableListOf<String>(),
        engine = defaultDefaultBrokerOverrideEngine(flags),
    )
}
