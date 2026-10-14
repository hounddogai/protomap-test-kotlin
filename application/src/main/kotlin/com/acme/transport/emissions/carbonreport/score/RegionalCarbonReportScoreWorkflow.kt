package com.acme.transport.emissions.carbonreport.score

import com.acme.transport.emissions.clock.Clock

/** Moves regional carbon report scores through their life cycle and keeps a journal of what it did. */
data class RegionalCarbonReportScoreService(
    var store: RegionalCarbonReportScoreStore,
    var validator: RegionalCarbonReportScoreValidator,
    var clock: Clock,
    var journal: MutableList<String>,
    var engine: RegionalCarbonReportScoreEngine,
) {
    private fun note(message: String) {
        journal.add("${clock.now()} $message")
    }

    fun start(code: String) {
        val item = store.find(code) ?: error("regional carbon report score $code not found")
        validator.requireValid(item)
        item.moveTo(RegionalCarbonReportScoreStatus.PENDING)
        store.save(item)
        note("$code is pending")
    }

    fun retire(code: String, reason: String) {
        val item = store.find(code) ?: error("regional carbon report score $code not found")
        if (item.isTerminal()) {
            note("$code already closed")
            return
        }
        item.moveTo(RegionalCarbonReportScoreStatus.CLOSED)
        store.save(item)
        note("$code is closed: $reason")
    }

    fun quote(code: String): Double {
        val item = store.find(code) ?: error("regional carbon report score $code not found")
        val result = engine.evaluate(item)
        note("$code evaluated at $result")
        return result
    }

    fun audit(cutoff: Int): Int {
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

fun newRegionalCarbonReportScoreService(store: RegionalCarbonReportScoreStore, clock: Clock): RegionalCarbonReportScoreService {
    return RegionalCarbonReportScoreService(
        store = store,
        validator = defaultRegionalCarbonReportScoreValidator(),
        clock = clock,
        journal = mutableListOf<String>(),
        engine = defaultRegionalCarbonReportScoreEngine(),
    )
}
