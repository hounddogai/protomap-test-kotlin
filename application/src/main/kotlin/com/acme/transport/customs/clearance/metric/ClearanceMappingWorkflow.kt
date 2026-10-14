package com.acme.transport.customs.clearance.metric

import com.acme.transport.customs.clock.Clock
import com.acme.transport.kit.flags.Registry

/** Moves clearance mappings through their life cycle and keeps a journal of what it did. */
data class ClearanceMappingService(
    var store: ClearanceMappingStore,
    var validator: ClearanceMappingValidator,
    var clock: Clock,
    var journal: MutableList<String>,
    var engine: ClearanceMappingEngine,
) {
    private fun note(message: String) {
        journal.add("${clock.now()} $message")
    }

    fun submit(code: String) {
        val item = store.find(code) ?: error("clearance mapping $code not found")
        validator.requireValid(item)
        item.moveTo(ClearanceMappingStatus.VALIDATED)
        store.save(item)
        note("$code is validated")
    }

    fun retire(code: String, reason: String) {
        val item = store.find(code) ?: error("clearance mapping $code not found")
        if (item.isTerminal()) {
            note("$code already closed")
            return
        }
        item.moveTo(ClearanceMappingStatus.RETIRED)
        store.save(item)
        note("$code is retired: $reason")
    }

    fun quote(code: String): Double {
        val item = store.find(code) ?: error("clearance mapping $code not found")
        val result = engine.price(item)
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

fun newClearanceMappingService(store: ClearanceMappingStore, clock: Clock, flags: Registry): ClearanceMappingService {
    return ClearanceMappingService(
        store = store,
        validator = defaultClearanceMappingValidator(),
        clock = clock,
        journal = mutableListOf<String>(),
        engine = defaultClearanceMappingEngine(flags),
    )
}
