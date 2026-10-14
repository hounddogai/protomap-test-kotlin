package com.acme.transport.customs.inspection.slot

import com.acme.transport.customs.clock.Clock
import com.acme.transport.kit.flags.Registry

/** Moves fallback inspection overrides through their life cycle and keeps a journal of what it did. */
data class FallbackInspectionOverrideCoordinator(
    var store: FallbackInspectionOverrideStore,
    var validator: FallbackInspectionOverrideValidator,
    var clock: Clock,
    var journal: MutableList<String>,
    var engine: FallbackInspectionOverrideEngine,
) {
    private fun note(message: String) {
        journal.add("${clock.now()} $message")
    }

    fun open(code: String) {
        val item = store.find(code) ?: error("fallback inspection override $code not found")
        validator.requireValid(item)
        item.moveTo(FallbackInspectionOverrideStatus.SCHEDULED)
        store.save(item)
        note("$code is scheduled")
    }

    fun retire(code: String, reason: String) {
        val item = store.find(code) ?: error("fallback inspection override $code not found")
        if (item.isTerminal()) {
            note("$code already closed")
            return
        }
        item.moveTo(FallbackInspectionOverrideStatus.CANCELLED)
        store.save(item)
        note("$code is cancelled: $reason")
    }

    fun quote(code: String): Double {
        val item = store.find(code) ?: error("fallback inspection override $code not found")
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

fun newFallbackInspectionOverrideCoordinator(store: FallbackInspectionOverrideStore, clock: Clock, flags: Registry): FallbackInspectionOverrideCoordinator {
    return FallbackInspectionOverrideCoordinator(
        store = store,
        validator = defaultFallbackInspectionOverrideValidator(),
        clock = clock,
        journal = mutableListOf<String>(),
        engine = defaultFallbackInspectionOverrideEngine(flags),
    )
}
