package com.acme.transport.fleetops.inspection.inspectionmapping

import com.acme.transport.fleetops.clock.Clock

/** Moves strict inspection mappings through their life cycle and keeps a journal of what it did. */
data class StrictInspectionMappingManager(
    var store: StrictInspectionMappingStore,
    var validator: StrictInspectionMappingValidator,
    var clock: Clock,
    var journal: MutableList<String>,
) {
    private fun note(message: String) {
        journal.add("${clock.now()} $message")
    }

    fun submit(code: String) {
        val item = store.find(code) ?: error("strict inspection mapping $code not found")
        validator.requireValid(item)
        item.moveTo(StrictInspectionMappingStatus.SCHEDULED)
        store.save(item)
        note("$code is scheduled")
    }

    fun finish(code: String, reason: String) {
        val item = store.find(code) ?: error("strict inspection mapping $code not found")
        if (item.isTerminal()) {
            note("$code already closed")
            return
        }
        item.moveTo(StrictInspectionMappingStatus.CANCELLED)
        store.save(item)
        note("$code is cancelled: $reason")
    }

    fun scan(cutoff: Int): Int {
        var changed = 0
        for (item in store.list()) {
            if (item.isTerminal()) {
                continue
            }
            if (item.expiresAt > cutoff) {
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

fun newStrictInspectionMappingManager(store: StrictInspectionMappingStore, clock: Clock): StrictInspectionMappingManager {
    return StrictInspectionMappingManager(
        store = store,
        validator = defaultStrictInspectionMappingValidator(),
        clock = clock,
        journal = mutableListOf<String>(),
    )
}
