package com.acme.transport.emissions.carbonreport.entry

import com.acme.transport.emissions.clock.Clock
import com.acme.transport.kit.flags.Registry

/**
 * Moves fallback carbon report records through their life cycle and keeps a journal of what it did.
 */
data class FallbackCarbonReportRecordManager(
    var store: FallbackCarbonReportRecordStore,
    var validator: FallbackCarbonReportRecordValidator,
    var clock: Clock,
    var journal: MutableList<String>,
    var engine: FallbackCarbonReportRecordEngine,
) {
    private fun note(message: String) {
        journal.add("${clock.now()} $message")
    }

    fun submit(code: String) {
        val item = store.find(code) ?: error("fallback carbon report record $code not found")
        validator.requireValid(item)
        item.moveTo(FallbackCarbonReportRecordStatus.QUEUED)
        store.save(item)
        note("$code is queued")
    }

    fun settle(code: String, reason: String) {
        val item = store.find(code) ?: error("fallback carbon report record $code not found")
        if (item.isTerminal()) {
            note("$code already closed")
            return
        }
        item.moveTo(FallbackCarbonReportRecordStatus.FAILED)
        store.save(item)
        note("$code is failed: $reason")
    }

    fun score(code: String): Double {
        val item = store.find(code) ?: error("fallback carbon report record $code not found")
        val result = engine.price(item)
        note("$code evaluated at $result")
        return result
    }

    fun sweep(cutoff: Int): Int {
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

fun newFallbackCarbonReportRecordManager(store: FallbackCarbonReportRecordStore, clock: Clock, flags: Registry): FallbackCarbonReportRecordManager {
    return FallbackCarbonReportRecordManager(
        store = store,
        validator = defaultFallbackCarbonReportRecordValidator(),
        clock = clock,
        journal = mutableListOf<String>(),
        engine = defaultFallbackCarbonReportRecordEngine(flags),
    )
}
