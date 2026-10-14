package com.acme.transport.emissions.evcharge.proposal

import com.acme.transport.emissions.clock.Clock

/** Moves regional ev charge metrics through their life cycle and keeps a journal of what it did. */
data class RegionalEvChargeMetricWorkflow(
    var store: RegionalEvChargeMetricStore,
    var validator: RegionalEvChargeMetricValidator,
    var clock: Clock,
    var journal: MutableList<String>,
) {
    private fun note(message: String) {
        journal.add("${clock.now()} $message")
    }

    fun start(code: String) {
        val item = store.find(code) ?: error("regional ev charge metric $code not found")
        validator.requireValid(item)
        item.moveTo(RegionalEvChargeMetricStatus.WARMING)
        store.save(item)
        note("$code is warming")
    }

    fun retire(code: String, reason: String) {
        val item = store.find(code) ?: error("regional ev charge metric $code not found")
        if (item.isTerminal()) {
            note("$code already closed")
            return
        }
        item.moveTo(RegionalEvChargeMetricStatus.STOPPED)
        store.save(item)
        note("$code is stopped: $reason")
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

fun newRegionalEvChargeMetricWorkflow(store: RegionalEvChargeMetricStore, clock: Clock): RegionalEvChargeMetricWorkflow {
    return RegionalEvChargeMetricWorkflow(
        store = store,
        validator = defaultRegionalEvChargeMetricValidator(),
        clock = clock,
        journal = mutableListOf<String>(),
    )
}
