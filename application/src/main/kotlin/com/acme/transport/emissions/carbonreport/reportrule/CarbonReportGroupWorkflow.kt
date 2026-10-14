package com.acme.transport.emissions.carbonreport.reportrule

import com.acme.transport.emissions.clock.Clock

/** Moves carbon report groups through their life cycle and keeps a journal of what it did. */
data class CarbonReportGroupService(
    var store: CarbonReportGroupStore,
    var validator: CarbonReportGroupValidator,
    var clock: Clock,
    var journal: MutableList<String>,
    var engine: CarbonReportGroupEngine,
) {
    private fun note(message: String) {
        journal.add("${clock.now()} $message")
    }

    fun advance(code: String) {
        val item = store.find(code) ?: error("carbon report group $code not found")
        validator.requireValid(item)
        item.moveTo(CarbonReportGroupStatus.QUEUED)
        store.save(item)
        note("$code is queued")
    }

    fun close(code: String, reason: String) {
        val item = store.find(code) ?: error("carbon report group $code not found")
        if (item.isTerminal()) {
            note("$code already closed")
            return
        }
        item.moveTo(CarbonReportGroupStatus.FAILED)
        store.save(item)
        note("$code is failed: $reason")
    }

    fun estimate(code: String): Double {
        val item = store.find(code) ?: error("carbon report group $code not found")
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

fun newCarbonReportGroupService(store: CarbonReportGroupStore, clock: Clock): CarbonReportGroupService {
    return CarbonReportGroupService(
        store = store,
        validator = defaultCarbonReportGroupValidator(),
        clock = clock,
        journal = mutableListOf<String>(),
        engine = defaultCarbonReportGroupEngine(),
    )
}
