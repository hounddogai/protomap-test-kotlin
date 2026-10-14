package com.acme.transport.emissions.carbonreport.mapping

import com.acme.transport.emissions.clock.Clock

/** Moves carbon report reviews through their life cycle and keeps a journal of what it did. */
data class CarbonReportReviewCoordinator(
    var store: CarbonReportReviewStore,
    var validator: CarbonReportReviewValidator,
    var clock: Clock,
    var journal: MutableList<String>,
    var engine: CarbonReportReviewEngine,
) {
    private fun note(message: String) {
        journal.add("${clock.now()} $message")
    }

    fun submit(code: String) {
        val item = store.find(code) ?: error("carbon report review $code not found")
        validator.requireValid(item)
        item.moveTo(CarbonReportReviewStatus.WARMING)
        store.save(item)
        note("$code is warming")
    }

    fun finish(code: String, reason: String) {
        val item = store.find(code) ?: error("carbon report review $code not found")
        if (item.isTerminal()) {
            note("$code already closed")
            return
        }
        item.moveTo(CarbonReportReviewStatus.STOPPED)
        store.save(item)
        note("$code is stopped: $reason")
    }

    fun quote(code: String): Double {
        val item = store.find(code) ?: error("carbon report review $code not found")
        val result = engine.price(item)
        note("$code evaluated at $result")
        return result
    }

    fun scan(cutoff: Int): Int {
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

fun newCarbonReportReviewCoordinator(store: CarbonReportReviewStore, clock: Clock): CarbonReportReviewCoordinator {
    return CarbonReportReviewCoordinator(
        store = store,
        validator = defaultCarbonReportReviewValidator(),
        clock = clock,
        journal = mutableListOf<String>(),
        engine = defaultCarbonReportReviewEngine(),
    )
}
