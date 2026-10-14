package com.acme.transport.network.transittime.digest

import com.acme.transport.kit.flags.Registry
import com.acme.transport.network.clock.Clock

/** Moves transit time variances through their life cycle and keeps a journal of what it did. */
data class TransitTimeVarianceManager(
    var store: TransitTimeVarianceStore,
    var validator: TransitTimeVarianceValidator,
    var clock: Clock,
    var journal: MutableList<String>,
    var engine: TransitTimeVarianceEngine,
) {
    private fun note(message: String) {
        journal.add("${clock.now()} $message")
    }

    fun submit(code: String) {
        val item = store.find(code) ?: error("transit time variance $code not found")
        validator.requireValid(item)
        item.moveTo(TransitTimeVarianceStatus.WARMING)
        store.save(item)
        note("$code is warming")
    }

    fun finish(code: String, reason: String) {
        val item = store.find(code) ?: error("transit time variance $code not found")
        if (item.isTerminal()) {
            note("$code already closed")
            return
        }
        item.moveTo(TransitTimeVarianceStatus.STOPPED)
        store.save(item)
        note("$code is stopped: $reason")
    }

    fun quote(code: String): Double {
        val item = store.find(code) ?: error("transit time variance $code not found")
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

fun newTransitTimeVarianceManager(store: TransitTimeVarianceStore, clock: Clock, flags: Registry): TransitTimeVarianceManager {
    return TransitTimeVarianceManager(
        store = store,
        validator = defaultTransitTimeVarianceValidator(),
        clock = clock,
        journal = mutableListOf<String>(),
        engine = defaultTransitTimeVarianceEngine(flags),
    )
}
