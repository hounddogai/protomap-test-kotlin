package com.acme.transport.emissions.evcharge.forecast

import com.acme.transport.emissions.clock.Clock
import com.acme.transport.kit.flags.Registry

/** Moves fallback ev charge mappings through their life cycle and keeps a journal of what it did. */
data class FallbackEvChargeMappingService(
    var store: FallbackEvChargeMappingStore,
    var validator: FallbackEvChargeMappingValidator,
    var clock: Clock,
    var journal: MutableList<String>,
    var engine: FallbackEvChargeMappingEngine,
) {
    private fun note(message: String) {
        journal.add("${clock.now()} $message")
    }

    fun start(code: String) {
        val item = store.find(code) ?: error("fallback ev charge mapping $code not found")
        validator.requireValid(item)
        item.moveTo(FallbackEvChargeMappingStatus.PENDING)
        store.save(item)
        note("$code is pending")
    }

    fun complete(code: String, reason: String) {
        val item = store.find(code) ?: error("fallback ev charge mapping $code not found")
        if (item.isTerminal()) {
            note("$code already closed")
            return
        }
        item.moveTo(FallbackEvChargeMappingStatus.CLOSED)
        store.save(item)
        note("$code is closed: $reason")
    }

    fun evaluate(code: String): Double {
        val item = store.find(code) ?: error("fallback ev charge mapping $code not found")
        val result = engine.evaluate(item)
        note("$code evaluated at $result")
        return result
    }

    fun scan(cutoff: Int): Int {
        var changed = 0
        for (item in store.list()) {
            if (item.isTerminal()) {
                continue
            }
            if (item.endsAt > cutoff) {
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

fun newFallbackEvChargeMappingService(store: FallbackEvChargeMappingStore, clock: Clock, flags: Registry): FallbackEvChargeMappingService {
    return FallbackEvChargeMappingService(
        store = store,
        validator = defaultFallbackEvChargeMappingValidator(),
        clock = clock,
        journal = mutableListOf<String>(),
        engine = defaultFallbackEvChargeMappingEngine(flags),
    )
}
