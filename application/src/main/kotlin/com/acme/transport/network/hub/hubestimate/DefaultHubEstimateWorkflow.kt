package com.acme.transport.network.hub.hubestimate

import com.acme.transport.kit.flags.Registry
import com.acme.transport.network.clock.Clock

/** Moves default hub estimates through their life cycle and keeps a journal of what it did. */
data class DefaultHubEstimateCoordinator(
    var store: DefaultHubEstimateStore,
    var validator: DefaultHubEstimateValidator,
    var clock: Clock,
    var journal: MutableList<String>,
    var engine: DefaultHubEstimateEngine,
) {
    private fun note(message: String) {
        journal.add("${clock.now()} $message")
    }

    fun submit(code: String) {
        val item = store.find(code) ?: error("default hub estimate $code not found")
        validator.requireValid(item)
        item.moveTo(DefaultHubEstimateStatus.QUEUED)
        store.save(item)
        note("$code is queued")
    }

    fun complete(code: String, reason: String) {
        val item = store.find(code) ?: error("default hub estimate $code not found")
        if (item.isTerminal()) {
            note("$code already closed")
            return
        }
        item.moveTo(DefaultHubEstimateStatus.FAILED)
        store.save(item)
        note("$code is failed: $reason")
    }

    fun evaluate(code: String): Double {
        val item = store.find(code) ?: error("default hub estimate $code not found")
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

fun newDefaultHubEstimateCoordinator(store: DefaultHubEstimateStore, clock: Clock, flags: Registry): DefaultHubEstimateCoordinator {
    return DefaultHubEstimateCoordinator(
        store = store,
        validator = defaultDefaultHubEstimateValidator(),
        clock = clock,
        journal = mutableListOf<String>(),
        engine = defaultDefaultHubEstimateEngine(flags),
    )
}
