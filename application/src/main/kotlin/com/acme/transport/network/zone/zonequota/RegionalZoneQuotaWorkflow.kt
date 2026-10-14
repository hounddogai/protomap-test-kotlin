package com.acme.transport.network.zone.zonequota

import com.acme.transport.kit.flags.Registry
import com.acme.transport.network.clock.Clock

/** Moves regional zone quotas through their life cycle and keeps a journal of what it did. */
data class RegionalZoneQuotaWorkflow(
    var store: RegionalZoneQuotaStore,
    var validator: RegionalZoneQuotaValidator,
    var clock: Clock,
    var journal: MutableList<String>,
    var engine: RegionalZoneQuotaEngine,
) {
    private fun note(message: String) {
        journal.add("${clock.now()} $message")
    }

    fun open(code: String) {
        val item = store.find(code) ?: error("regional zone quota $code not found")
        validator.requireValid(item)
        item.moveTo(RegionalZoneQuotaStatus.VALIDATED)
        store.save(item)
        note("$code is validated")
    }

    fun settle(code: String, reason: String) {
        val item = store.find(code) ?: error("regional zone quota $code not found")
        if (item.isTerminal()) {
            note("$code already closed")
            return
        }
        item.moveTo(RegionalZoneQuotaStatus.RETIRED)
        store.save(item)
        note("$code is retired: $reason")
    }

    fun quote(code: String): Double {
        val item = store.find(code) ?: error("regional zone quota $code not found")
        val result = engine.compute(item)
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

fun newRegionalZoneQuotaWorkflow(store: RegionalZoneQuotaStore, clock: Clock, flags: Registry): RegionalZoneQuotaWorkflow {
    return RegionalZoneQuotaWorkflow(
        store = store,
        validator = defaultRegionalZoneQuotaValidator(),
        clock = clock,
        journal = mutableListOf<String>(),
        engine = defaultRegionalZoneQuotaEngine(flags),
    )
}
