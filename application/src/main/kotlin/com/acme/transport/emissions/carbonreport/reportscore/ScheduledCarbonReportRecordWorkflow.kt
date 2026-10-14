package com.acme.transport.emissions.carbonreport.reportscore

import com.acme.transport.emissions.clock.Clock

/**
 * Moves scheduled carbon report records through their life cycle and keeps a journal of what it did.
 */
data class ScheduledCarbonReportRecordManager(
    var store: ScheduledCarbonReportRecordStore,
    var validator: ScheduledCarbonReportRecordValidator,
    var clock: Clock,
    var journal: MutableList<String>,
    var engine: ScheduledCarbonReportRecordEngine,
) {
    private fun note(message: String) {
        journal.add("${clock.now()} $message")
    }

    fun start(code: String) {
        val item = store.find(code) ?: error("scheduled carbon report record $code not found")
        validator.requireValid(item)
        item.moveTo(ScheduledCarbonReportRecordStatus.PENDING)
        store.save(item)
        note("$code is pending")
    }

    fun retire(code: String, reason: String) {
        val item = store.find(code) ?: error("scheduled carbon report record $code not found")
        if (item.isTerminal()) {
            note("$code already closed")
            return
        }
        item.moveTo(ScheduledCarbonReportRecordStatus.CLOSED)
        store.save(item)
        note("$code is closed: $reason")
    }

    fun quote(code: String): Double {
        val item = store.find(code) ?: error("scheduled carbon report record $code not found")
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

fun newScheduledCarbonReportRecordManager(store: ScheduledCarbonReportRecordStore, clock: Clock): ScheduledCarbonReportRecordManager {
    return ScheduledCarbonReportRecordManager(
        store = store,
        validator = defaultScheduledCarbonReportRecordValidator(),
        clock = clock,
        journal = mutableListOf<String>(),
        engine = defaultScheduledCarbonReportRecordEngine(),
    )
}
