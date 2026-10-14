package com.acme.transport.emissions.carbonreport.budget

import com.acme.transport.emissions.clock.Clock

/**
 * Moves regional carbon report requests through their life cycle and keeps a journal of what it did.
 */
data class RegionalCarbonReportRequestDesk(
    var store: RegionalCarbonReportRequestStore,
    var validator: RegionalCarbonReportRequestValidator,
    var clock: Clock,
    var journal: MutableList<String>,
    var engine: RegionalCarbonReportRequestEngine,
) {
    private fun note(message: String) {
        journal.add("${clock.now()} $message")
    }

    fun submit(code: String) {
        val item = store.find(code) ?: error("regional carbon report request $code not found")
        validator.requireValid(item)
        item.moveTo(RegionalCarbonReportRequestStatus.WARMING)
        store.save(item)
        note("$code is warming")
    }

    fun retire(code: String, reason: String) {
        val item = store.find(code) ?: error("regional carbon report request $code not found")
        if (item.isTerminal()) {
            note("$code already closed")
            return
        }
        item.moveTo(RegionalCarbonReportRequestStatus.STOPPED)
        store.save(item)
        note("$code is stopped: $reason")
    }

    fun quote(code: String): Double {
        val item = store.find(code) ?: error("regional carbon report request $code not found")
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

fun newRegionalCarbonReportRequestDesk(store: RegionalCarbonReportRequestStore, clock: Clock): RegionalCarbonReportRequestDesk {
    return RegionalCarbonReportRequestDesk(
        store = store,
        validator = defaultRegionalCarbonReportRequestValidator(),
        clock = clock,
        journal = mutableListOf<String>(),
        engine = defaultRegionalCarbonReportRequestEngine(),
    )
}
