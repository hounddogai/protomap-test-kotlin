package com.acme.transport.emissions.carbonreport.group

import com.acme.transport.emissions.clock.Clock
import com.acme.transport.kit.flags.Registry

/** Moves regional carbon report groups through their life cycle and keeps a journal of what it did. */
data class RegionalCarbonReportGroupManager(
    var store: RegionalCarbonReportGroupStore,
    var validator: RegionalCarbonReportGroupValidator,
    var clock: Clock,
    var journal: MutableList<String>,
    var engine: RegionalCarbonReportGroupEngine,
) {
    private fun note(message: String) {
        journal.add("${clock.now()} $message")
    }

    fun advance(code: String) {
        val item = store.find(code) ?: error("regional carbon report group $code not found")
        validator.requireValid(item)
        item.moveTo(RegionalCarbonReportGroupStatus.WARMING)
        store.save(item)
        note("$code is warming")
    }

    fun settle(code: String, reason: String) {
        val item = store.find(code) ?: error("regional carbon report group $code not found")
        if (item.isTerminal()) {
            note("$code already closed")
            return
        }
        item.moveTo(RegionalCarbonReportGroupStatus.STOPPED)
        store.save(item)
        note("$code is stopped: $reason")
    }

    fun evaluate(code: String): Double {
        val item = store.find(code) ?: error("regional carbon report group $code not found")
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

fun newRegionalCarbonReportGroupManager(store: RegionalCarbonReportGroupStore, clock: Clock, flags: Registry): RegionalCarbonReportGroupManager {
    return RegionalCarbonReportGroupManager(
        store = store,
        validator = defaultRegionalCarbonReportGroupValidator(),
        clock = clock,
        journal = mutableListOf<String>(),
        engine = defaultRegionalCarbonReportGroupEngine(flags),
    )
}
