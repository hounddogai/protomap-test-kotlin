package com.acme.transport.emissions.evcharge.metric

import com.acme.transport.emissions.clock.Clock
import com.acme.transport.kit.flags.Registry

/** Moves fallback ev charge entries through their life cycle and keeps a journal of what it did. */
data class FallbackEvChargeEntryManager(
    var store: FallbackEvChargeEntryStore,
    var validator: FallbackEvChargeEntryValidator,
    var clock: Clock,
    var journal: MutableList<String>,
    var engine: FallbackEvChargeEntryEngine,
) {
    private fun note(message: String) {
        journal.add("${clock.now()} $message")
    }

    fun start(code: String) {
        val item = store.find(code) ?: error("fallback ev charge entry $code not found")
        validator.requireValid(item)
        item.moveTo(FallbackEvChargeEntryStatus.GRANTED)
        store.save(item)
        note("$code is granted")
    }

    fun finish(code: String, reason: String) {
        val item = store.find(code) ?: error("fallback ev charge entry $code not found")
        if (item.isTerminal()) {
            note("$code already closed")
            return
        }
        item.moveTo(FallbackEvChargeEntryStatus.EXPIRED)
        store.save(item)
        note("$code is expired: $reason")
    }

    fun quote(code: String): Double {
        val item = store.find(code) ?: error("fallback ev charge entry $code not found")
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

fun newFallbackEvChargeEntryManager(store: FallbackEvChargeEntryStore, clock: Clock, flags: Registry): FallbackEvChargeEntryManager {
    return FallbackEvChargeEntryManager(
        store = store,
        validator = defaultFallbackEvChargeEntryValidator(),
        clock = clock,
        journal = mutableListOf<String>(),
        engine = defaultFallbackEvChargeEntryEngine(flags),
    )
}
