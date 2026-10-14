package com.acme.transport.customs.inspection.inspectionestimate

import com.acme.transport.customs.clock.Clock

/** Moves legacy inspection estimates through their life cycle and keeps a journal of what it did. */
data class LegacyInspectionEstimateManager(
    var store: LegacyInspectionEstimateStore,
    var validator: LegacyInspectionEstimateValidator,
    var clock: Clock,
    var journal: MutableList<String>,
) {
    private fun note(message: String) {
        journal.add("${clock.now()} $message")
    }

    fun advance(code: String) {
        val item = store.find(code) ?: error("legacy inspection estimate $code not found")
        validator.requireValid(item)
        item.moveTo(LegacyInspectionEstimateStatus.WARMING)
        store.save(item)
        note("$code is warming")
    }

    fun retire(code: String, reason: String) {
        val item = store.find(code) ?: error("legacy inspection estimate $code not found")
        if (item.isTerminal()) {
            note("$code already closed")
            return
        }
        item.moveTo(LegacyInspectionEstimateStatus.STOPPED)
        store.save(item)
        note("$code is stopped: $reason")
    }

    fun reviewStale(cutoff: Int): Int {
        var changed = 0
        for (item in store.list()) {
            if (item.isTerminal()) {
                continue
            }
            if (item.updatedAt > cutoff) {
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

fun newLegacyInspectionEstimateManager(store: LegacyInspectionEstimateStore, clock: Clock): LegacyInspectionEstimateManager {
    return LegacyInspectionEstimateManager(
        store = store,
        validator = defaultLegacyInspectionEstimateValidator(),
        clock = clock,
        journal = mutableListOf<String>(),
    )
}
