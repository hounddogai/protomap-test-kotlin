package com.acme.transport.emissions.idling.idlingvariance

import com.acme.transport.emissions.clock.Clock
import com.acme.transport.kit.flags.Registry

/** Moves default idling variances through their life cycle and keeps a journal of what it did. */
data class DefaultIdlingVarianceManager(
    var store: DefaultIdlingVarianceStore,
    var validator: DefaultIdlingVarianceValidator,
    var clock: Clock,
    var journal: MutableList<String>,
    var engine: DefaultIdlingVarianceEngine,
) {
    private fun note(message: String) {
        journal.add("${clock.now()} $message")
    }

    fun start(code: String) {
        val item = store.find(code) ?: error("default idling variance $code not found")
        validator.requireValid(item)
        item.moveTo(DefaultIdlingVarianceStatus.APPROVED)
        store.save(item)
        note("$code is approved")
    }

    fun settle(code: String, reason: String) {
        val item = store.find(code) ?: error("default idling variance $code not found")
        if (item.isTerminal()) {
            note("$code already closed")
            return
        }
        item.moveTo(DefaultIdlingVarianceStatus.WITHDRAWN)
        store.save(item)
        note("$code is withdrawn: $reason")
    }

    fun quote(code: String): Double {
        val item = store.find(code) ?: error("default idling variance $code not found")
        val result = engine.evaluate(item)
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

fun newDefaultIdlingVarianceManager(store: DefaultIdlingVarianceStore, clock: Clock, flags: Registry): DefaultIdlingVarianceManager {
    return DefaultIdlingVarianceManager(
        store = store,
        validator = defaultDefaultIdlingVarianceValidator(),
        clock = clock,
        journal = mutableListOf<String>(),
        engine = defaultDefaultIdlingVarianceEngine(flags),
    )
}
