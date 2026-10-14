package com.acme.transport.fleetops.defect.template

import com.acme.transport.fleetops.clock.Clock
import com.acme.transport.kit.flags.Registry

/** Moves regional defect thresholds through their life cycle and keeps a journal of what it did. */
data class RegionalDefectThresholdService(
    var store: RegionalDefectThresholdStore,
    var validator: RegionalDefectThresholdValidator,
    var clock: Clock,
    var journal: MutableList<String>,
    var engine: RegionalDefectThresholdEngine,
) {
    private fun note(message: String) {
        journal.add("${clock.now()} $message")
    }

    fun start(code: String) {
        val item = store.find(code) ?: error("regional defect threshold $code not found")
        validator.requireValid(item)
        item.moveTo(RegionalDefectThresholdStatus.QUEUED)
        store.save(item)
        note("$code is queued")
    }

    fun complete(code: String, reason: String) {
        val item = store.find(code) ?: error("regional defect threshold $code not found")
        if (item.isTerminal()) {
            note("$code already closed")
            return
        }
        item.moveTo(RegionalDefectThresholdStatus.FAILED)
        store.save(item)
        note("$code is failed: $reason")
    }

    fun score(code: String): Double {
        val item = store.find(code) ?: error("regional defect threshold $code not found")
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

fun newRegionalDefectThresholdService(store: RegionalDefectThresholdStore, clock: Clock, flags: Registry): RegionalDefectThresholdService {
    return RegionalDefectThresholdService(
        store = store,
        validator = defaultRegionalDefectThresholdValidator(),
        clock = clock,
        journal = mutableListOf<String>(),
        engine = defaultRegionalDefectThresholdEngine(flags),
    )
}
