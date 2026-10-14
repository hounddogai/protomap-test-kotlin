package com.acme.transport.fleetops.inspection.mapping

import com.acme.transport.fleetops.clock.Clock

/** Moves capped inspection mappings through their life cycle and keeps a journal of what it did. */
data class CappedInspectionMappingManager(
    var store: CappedInspectionMappingStore,
    var validator: CappedInspectionMappingValidator,
    var clock: Clock,
    var journal: MutableList<String>,
    var engine: CappedInspectionMappingEngine,
) {
    private fun note(message: String) {
        journal.add("${clock.now()} $message")
    }

    fun start(code: String) {
        val item = store.find(code) ?: error("capped inspection mapping $code not found")
        validator.requireValid(item)
        item.moveTo(CappedInspectionMappingStatus.GRANTED)
        store.save(item)
        note("$code is granted")
    }

    fun finish(code: String, reason: String) {
        val item = store.find(code) ?: error("capped inspection mapping $code not found")
        if (item.isTerminal()) {
            note("$code already closed")
            return
        }
        item.moveTo(CappedInspectionMappingStatus.EXPIRED)
        store.save(item)
        note("$code is expired: $reason")
    }

    fun estimate(code: String): Double {
        val item = store.find(code) ?: error("capped inspection mapping $code not found")
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

fun newCappedInspectionMappingManager(store: CappedInspectionMappingStore, clock: Clock): CappedInspectionMappingManager {
    return CappedInspectionMappingManager(
        store = store,
        validator = defaultCappedInspectionMappingValidator(),
        clock = clock,
        journal = mutableListOf<String>(),
        engine = defaultCappedInspectionMappingEngine(),
    )
}
