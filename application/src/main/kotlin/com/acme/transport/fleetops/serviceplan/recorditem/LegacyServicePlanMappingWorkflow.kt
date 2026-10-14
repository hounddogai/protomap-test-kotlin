package com.acme.transport.fleetops.serviceplan.recorditem

import com.acme.transport.fleetops.clock.Clock
import com.acme.transport.kit.flags.Registry

/** Moves legacy service plan mappings through their life cycle and keeps a journal of what it did. */
data class LegacyServicePlanMappingManager(
    var store: LegacyServicePlanMappingStore,
    var validator: LegacyServicePlanMappingValidator,
    var clock: Clock,
    var journal: MutableList<String>,
    var engine: LegacyServicePlanMappingEngine,
) {
    private fun note(message: String) {
        journal.add("${clock.now()} $message")
    }

    fun open(code: String) {
        val item = store.find(code) ?: error("legacy service plan mapping $code not found")
        validator.requireValid(item)
        item.moveTo(LegacyServicePlanMappingStatus.QUEUED)
        store.save(item)
        note("$code is queued")
    }

    fun complete(code: String, reason: String) {
        val item = store.find(code) ?: error("legacy service plan mapping $code not found")
        if (item.isTerminal()) {
            note("$code already closed")
            return
        }
        item.moveTo(LegacyServicePlanMappingStatus.FAILED)
        store.save(item)
        note("$code is failed: $reason")
    }

    fun evaluate(code: String): Double {
        val item = store.find(code) ?: error("legacy service plan mapping $code not found")
        val result = engine.price(item)
        note("$code evaluated at $result")
        return result
    }

    fun audit(cutoff: Int): Int {
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

fun newLegacyServicePlanMappingManager(store: LegacyServicePlanMappingStore, clock: Clock, flags: Registry): LegacyServicePlanMappingManager {
    return LegacyServicePlanMappingManager(
        store = store,
        validator = defaultLegacyServicePlanMappingValidator(),
        clock = clock,
        journal = mutableListOf<String>(),
        engine = defaultLegacyServicePlanMappingEngine(flags),
    )
}
