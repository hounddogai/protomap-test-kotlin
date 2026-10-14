package com.acme.transport.crewing.availability.manifest

import com.acme.transport.crewing.clock.Clock
import com.acme.transport.kit.flags.Registry

/**
 * Moves adaptive availability mappings through their life cycle and keeps a journal of what it did.
 */
data class AdaptiveAvailabilityMappingService(
    var store: AdaptiveAvailabilityMappingStore,
    var validator: AdaptiveAvailabilityMappingValidator,
    var clock: Clock,
    var journal: MutableList<String>,
    var engine: AdaptiveAvailabilityMappingEngine,
) {
    private fun note(message: String) {
        journal.add("${clock.now()} $message")
    }

    fun start(code: String) {
        val item = store.find(code) ?: error("adaptive availability mapping $code not found")
        validator.requireValid(item)
        item.moveTo(AdaptiveAvailabilityMappingStatus.GRANTED)
        store.save(item)
        note("$code is granted")
    }

    fun finish(code: String, reason: String) {
        val item = store.find(code) ?: error("adaptive availability mapping $code not found")
        if (item.isTerminal()) {
            note("$code already closed")
            return
        }
        item.moveTo(AdaptiveAvailabilityMappingStatus.EXPIRED)
        store.save(item)
        note("$code is expired: $reason")
    }

    fun quote(code: String): Double {
        val item = store.find(code) ?: error("adaptive availability mapping $code not found")
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

fun newAdaptiveAvailabilityMappingService(store: AdaptiveAvailabilityMappingStore, clock: Clock, flags: Registry): AdaptiveAvailabilityMappingService {
    return AdaptiveAvailabilityMappingService(
        store = store,
        validator = defaultAdaptiveAvailabilityMappingValidator(),
        clock = clock,
        journal = mutableListOf<String>(),
        engine = defaultAdaptiveAvailabilityMappingEngine(flags),
    )
}
