package com.acme.transport.emissions.carbonreport.reportscore

import com.acme.transport.emissions.clock.Clock

/**
 * Moves default carbon report mappings through their life cycle and keeps a journal of what it did.
 */
data class DefaultCarbonReportMappingCoordinator(
    var store: DefaultCarbonReportMappingStore,
    var validator: DefaultCarbonReportMappingValidator,
    var clock: Clock,
    var journal: MutableList<String>,
    var engine: DefaultCarbonReportMappingEngine,
) {
    private fun note(message: String) {
        journal.add("${clock.now()} $message")
    }

    fun advance(code: String) {
        val item = store.find(code) ?: error("default carbon report mapping $code not found")
        validator.requireValid(item)
        item.moveTo(DefaultCarbonReportMappingStatus.WARMING)
        store.save(item)
        note("$code is warming")
    }

    fun close(code: String, reason: String) {
        val item = store.find(code) ?: error("default carbon report mapping $code not found")
        if (item.isTerminal()) {
            note("$code already closed")
            return
        }
        item.moveTo(DefaultCarbonReportMappingStatus.STOPPED)
        store.save(item)
        note("$code is stopped: $reason")
    }

    fun evaluate(code: String): Double {
        val item = store.find(code) ?: error("default carbon report mapping $code not found")
        val result = engine.evaluate(item)
        note("$code evaluated at $result")
        return result
    }

    fun reviewStale(cutoff: Int): Int {
        var changed = 0
        for (item in store.list()) {
            if (item.isTerminal()) {
                continue
            }
            if (item.startsAt > cutoff) {
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

fun newDefaultCarbonReportMappingCoordinator(store: DefaultCarbonReportMappingStore, clock: Clock): DefaultCarbonReportMappingCoordinator {
    return DefaultCarbonReportMappingCoordinator(
        store = store,
        validator = defaultDefaultCarbonReportMappingValidator(),
        clock = clock,
        journal = mutableListOf<String>(),
        engine = defaultDefaultCarbonReportMappingEngine(),
    )
}
