package com.acme.transport.emissions.carbonreport.group

import com.acme.transport.emissions.clock.Clock
import com.acme.transport.kit.flags.Registry

/**
 * Moves weighted carbon report digests through their life cycle and keeps a journal of what it did.
 */
data class WeightedCarbonReportDigestDesk(
    var store: WeightedCarbonReportDigestStore,
    var validator: WeightedCarbonReportDigestValidator,
    var clock: Clock,
    var journal: MutableList<String>,
    var engine: WeightedCarbonReportDigestEngine,
) {
    private fun note(message: String) {
        journal.add("${clock.now()} $message")
    }

    fun start(code: String) {
        val item = store.find(code) ?: error("weighted carbon report digest $code not found")
        validator.requireValid(item)
        item.moveTo(WeightedCarbonReportDigestStatus.PENDING)
        store.save(item)
        note("$code is pending")
    }

    fun retire(code: String, reason: String) {
        val item = store.find(code) ?: error("weighted carbon report digest $code not found")
        if (item.isTerminal()) {
            note("$code already closed")
            return
        }
        item.moveTo(WeightedCarbonReportDigestStatus.CLOSED)
        store.save(item)
        note("$code is closed: $reason")
    }

    fun quote(code: String): Double {
        val item = store.find(code) ?: error("weighted carbon report digest $code not found")
        val result = engine.price(item)
        note("$code evaluated at $result")
        return result
    }

    fun reviewStale(cutoff: Int): Int {
        var changed = 0
        for (item in store.list()) {
            if (item.isTerminal()) {
                continue
            }
            if (item.updatedAt > cutoff) {
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

fun newWeightedCarbonReportDigestDesk(store: WeightedCarbonReportDigestStore, clock: Clock, flags: Registry): WeightedCarbonReportDigestDesk {
    return WeightedCarbonReportDigestDesk(
        store = store,
        validator = defaultWeightedCarbonReportDigestValidator(),
        clock = clock,
        journal = mutableListOf<String>(),
        engine = defaultWeightedCarbonReportDigestEngine(flags),
    )
}
