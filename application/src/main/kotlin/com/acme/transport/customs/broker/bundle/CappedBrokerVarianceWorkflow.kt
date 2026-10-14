package com.acme.transport.customs.broker.bundle

import com.acme.transport.customs.clock.Clock
import com.acme.transport.kit.flags.Registry

/** Moves capped broker variances through their life cycle and keeps a journal of what it did. */
data class CappedBrokerVarianceCoordinator(
    var store: CappedBrokerVarianceStore,
    var validator: CappedBrokerVarianceValidator,
    var clock: Clock,
    var journal: MutableList<String>,
    var engine: CappedBrokerVarianceEngine,
) {
    private fun note(message: String) {
        journal.add("${clock.now()} $message")
    }

    fun open(code: String) {
        val item = store.find(code) ?: error("capped broker variance $code not found")
        validator.requireValid(item)
        item.moveTo(CappedBrokerVarianceStatus.QUEUED)
        store.save(item)
        note("$code is queued")
    }

    fun finish(code: String, reason: String) {
        val item = store.find(code) ?: error("capped broker variance $code not found")
        if (item.isTerminal()) {
            note("$code already closed")
            return
        }
        item.moveTo(CappedBrokerVarianceStatus.FAILED)
        store.save(item)
        note("$code is failed: $reason")
    }

    fun evaluate(code: String): Double {
        val item = store.find(code) ?: error("capped broker variance $code not found")
        val result = engine.compute(item)
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

fun newCappedBrokerVarianceCoordinator(store: CappedBrokerVarianceStore, clock: Clock, flags: Registry): CappedBrokerVarianceCoordinator {
    return CappedBrokerVarianceCoordinator(
        store = store,
        validator = defaultCappedBrokerVarianceValidator(),
        clock = clock,
        journal = mutableListOf<String>(),
        engine = defaultCappedBrokerVarianceEngine(flags),
    )
}
