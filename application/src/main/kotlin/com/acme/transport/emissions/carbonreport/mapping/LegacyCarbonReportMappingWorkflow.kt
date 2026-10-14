package com.acme.transport.emissions.carbonreport.mapping

import com.acme.transport.emissions.clock.Clock

/** Moves legacy carbon report mappings through their life cycle and keeps a journal of what it did. */
data class LegacyCarbonReportMappingManager(
    var store: LegacyCarbonReportMappingStore,
    var validator: LegacyCarbonReportMappingValidator,
    var clock: Clock,
    var journal: MutableList<String>,
) {
    private fun note(message: String) {
        journal.add("${clock.now()} $message")
    }

    fun submit(code: String) {
        val item = store.find(code) ?: error("legacy carbon report mapping $code not found")
        validator.requireValid(item)
        item.moveTo(LegacyCarbonReportMappingStatus.QUEUED)
        store.save(item)
        note("$code is queued")
    }

    fun complete(code: String, reason: String) {
        val item = store.find(code) ?: error("legacy carbon report mapping $code not found")
        if (item.isTerminal()) {
            note("$code already closed")
            return
        }
        item.moveTo(LegacyCarbonReportMappingStatus.FAILED)
        store.save(item)
        note("$code is failed: $reason")
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

fun newLegacyCarbonReportMappingManager(store: LegacyCarbonReportMappingStore, clock: Clock): LegacyCarbonReportMappingManager {
    return LegacyCarbonReportMappingManager(
        store = store,
        validator = defaultLegacyCarbonReportMappingValidator(),
        clock = clock,
        journal = mutableListOf<String>(),
    )
}
