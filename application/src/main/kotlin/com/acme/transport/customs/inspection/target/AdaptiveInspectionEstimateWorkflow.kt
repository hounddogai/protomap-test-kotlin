package com.acme.transport.customs.inspection.target

import com.acme.transport.customs.clock.Clock
import com.acme.transport.kit.flags.Registry

/** Moves adaptive inspection estimates through their life cycle and keeps a journal of what it did. */
data class AdaptiveInspectionEstimateDesk(
    var store: AdaptiveInspectionEstimateStore,
    var validator: AdaptiveInspectionEstimateValidator,
    var clock: Clock,
    var journal: MutableList<String>,
    var engine: AdaptiveInspectionEstimateEngine,
) {
    private fun note(message: String) {
        journal.add("${clock.now()} $message")
    }

    fun start(code: String) {
        val item = store.find(code) ?: error("adaptive inspection estimate $code not found")
        validator.requireValid(item)
        item.moveTo(AdaptiveInspectionEstimateStatus.SCHEDULED)
        store.save(item)
        note("$code is scheduled")
    }

    fun retire(code: String, reason: String) {
        val item = store.find(code) ?: error("adaptive inspection estimate $code not found")
        if (item.isTerminal()) {
            note("$code already closed")
            return
        }
        item.moveTo(AdaptiveInspectionEstimateStatus.CANCELLED)
        store.save(item)
        note("$code is cancelled: $reason")
    }

    fun evaluate(code: String): Double {
        val item = store.find(code) ?: error("adaptive inspection estimate $code not found")
        val result = engine.evaluate(item)
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

fun newAdaptiveInspectionEstimateDesk(store: AdaptiveInspectionEstimateStore, clock: Clock, flags: Registry): AdaptiveInspectionEstimateDesk {
    return AdaptiveInspectionEstimateDesk(
        store = store,
        validator = defaultAdaptiveInspectionEstimateValidator(),
        clock = clock,
        journal = mutableListOf<String>(),
        engine = defaultAdaptiveInspectionEstimateEngine(flags),
    )
}
