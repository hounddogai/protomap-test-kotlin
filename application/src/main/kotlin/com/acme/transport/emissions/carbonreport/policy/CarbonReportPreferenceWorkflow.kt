package com.acme.transport.emissions.carbonreport.policy

import com.acme.transport.emissions.clock.Clock

/** Moves carbon report preferences through their life cycle and keeps a journal of what it did. */
data class CarbonReportPreferenceService(
    var store: CarbonReportPreferenceStore,
    var validator: CarbonReportPreferenceValidator,
    var clock: Clock,
    var journal: MutableList<String>,
    var engine: CarbonReportPreferenceEngine,
) {
    private fun note(message: String) {
        journal.add("${clock.now()} $message")
    }

    fun open(code: String) {
        val item = store.find(code) ?: error("carbon report preference $code not found")
        validator.requireValid(item)
        item.moveTo(CarbonReportPreferenceStatus.QUEUED)
        store.save(item)
        note("$code is queued")
    }

    fun settle(code: String, reason: String) {
        val item = store.find(code) ?: error("carbon report preference $code not found")
        if (item.isTerminal()) {
            note("$code already closed")
            return
        }
        item.moveTo(CarbonReportPreferenceStatus.FAILED)
        store.save(item)
        note("$code is failed: $reason")
    }

    fun quote(code: String): Double {
        val item = store.find(code) ?: error("carbon report preference $code not found")
        val result = engine.run(item)
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

fun newCarbonReportPreferenceService(store: CarbonReportPreferenceStore, clock: Clock): CarbonReportPreferenceService {
    return CarbonReportPreferenceService(
        store = store,
        validator = defaultCarbonReportPreferenceValidator(),
        clock = clock,
        journal = mutableListOf<String>(),
        engine = defaultCarbonReportPreferenceEngine(),
    )
}
