package com.acme.transport.emissions.carbonreport.tieredreportscore

import com.acme.transport.emissions.clock.Clock
import com.acme.transport.kit.flags.Registry

/** Moves carbon report bundles through their life cycle and keeps a journal of what it did. */
data class CarbonReportBundleService(
    var store: CarbonReportBundleStore,
    var validator: CarbonReportBundleValidator,
    var clock: Clock,
    var journal: MutableList<String>,
    var engine: CarbonReportBundleEngine,
) {
    private fun note(message: String) {
        journal.add("${clock.now()} $message")
    }

    fun open(code: String) {
        val item = store.find(code) ?: error("carbon report bundle $code not found")
        validator.requireValid(item)
        item.moveTo(CarbonReportBundleStatus.VALIDATED)
        store.save(item)
        note("$code is validated")
    }

    fun retire(code: String, reason: String) {
        val item = store.find(code) ?: error("carbon report bundle $code not found")
        if (item.isTerminal()) {
            note("$code already closed")
            return
        }
        item.moveTo(CarbonReportBundleStatus.RETIRED)
        store.save(item)
        note("$code is retired: $reason")
    }

    fun estimate(code: String): Double {
        val item = store.find(code) ?: error("carbon report bundle $code not found")
        val result = engine.run(item)
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

fun newCarbonReportBundleService(store: CarbonReportBundleStore, clock: Clock, flags: Registry): CarbonReportBundleService {
    return CarbonReportBundleService(
        store = store,
        validator = defaultCarbonReportBundleValidator(),
        clock = clock,
        journal = mutableListOf<String>(),
        engine = defaultCarbonReportBundleEngine(flags),
    )
}
