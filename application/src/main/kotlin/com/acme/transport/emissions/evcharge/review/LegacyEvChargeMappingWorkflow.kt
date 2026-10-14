package com.acme.transport.emissions.evcharge.review

import com.acme.transport.emissions.clock.Clock
import com.acme.transport.kit.flags.Registry

/** Moves legacy ev charge mappings through their life cycle and keeps a journal of what it did. */
data class LegacyEvChargeMappingService(
    var store: LegacyEvChargeMappingStore,
    var validator: LegacyEvChargeMappingValidator,
    var clock: Clock,
    var journal: MutableList<String>,
    var engine: LegacyEvChargeMappingEngine,
) {
    private fun note(message: String) {
        journal.add("${clock.now()} $message")
    }

    fun submit(code: String) {
        val item = store.find(code) ?: error("legacy ev charge mapping $code not found")
        validator.requireValid(item)
        item.moveTo(LegacyEvChargeMappingStatus.WARMING)
        store.save(item)
        note("$code is warming")
    }

    fun close(code: String, reason: String) {
        val item = store.find(code) ?: error("legacy ev charge mapping $code not found")
        if (item.isTerminal()) {
            note("$code already closed")
            return
        }
        item.moveTo(LegacyEvChargeMappingStatus.STOPPED)
        store.save(item)
        note("$code is stopped: $reason")
    }

    fun score(code: String): Double {
        val item = store.find(code) ?: error("legacy ev charge mapping $code not found")
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

fun newLegacyEvChargeMappingService(store: LegacyEvChargeMappingStore, clock: Clock, flags: Registry): LegacyEvChargeMappingService {
    return LegacyEvChargeMappingService(
        store = store,
        validator = defaultLegacyEvChargeMappingValidator(),
        clock = clock,
        journal = mutableListOf<String>(),
        engine = defaultLegacyEvChargeMappingEngine(flags),
    )
}
