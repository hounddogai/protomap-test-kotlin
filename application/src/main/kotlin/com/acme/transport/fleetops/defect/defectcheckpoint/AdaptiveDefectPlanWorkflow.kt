package com.acme.transport.fleetops.defect.defectcheckpoint

import com.acme.transport.fleetops.clock.Clock
import com.acme.transport.kit.flags.Registry

/** Moves adaptive defect plans through their life cycle and keeps a journal of what it did. */
data class AdaptiveDefectPlanManager(
    var store: AdaptiveDefectPlanStore,
    var validator: AdaptiveDefectPlanValidator,
    var clock: Clock,
    var journal: MutableList<String>,
    var engine: AdaptiveDefectPlanEngine,
) {
    private fun note(message: String) {
        journal.add("${clock.now()} $message")
    }

    fun advance(code: String) {
        val item = store.find(code) ?: error("adaptive defect plan $code not found")
        validator.requireValid(item)
        item.moveTo(AdaptiveDefectPlanStatus.PENDING)
        store.save(item)
        note("$code is pending")
    }

    fun settle(code: String, reason: String) {
        val item = store.find(code) ?: error("adaptive defect plan $code not found")
        if (item.isTerminal()) {
            note("$code already closed")
            return
        }
        item.moveTo(AdaptiveDefectPlanStatus.CLOSED)
        store.save(item)
        note("$code is closed: $reason")
    }

    fun quote(code: String): Double {
        val item = store.find(code) ?: error("adaptive defect plan $code not found")
        val result = engine.run(item)
        note("$code evaluated at $result")
        return result
    }

    fun scan(cutoff: Int): Int {
        var changed = 0
        for (item in store.list()) {
            if (item.isTerminal()) {
                continue
            }
            if (item.endsAt > cutoff) {
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

fun newAdaptiveDefectPlanManager(store: AdaptiveDefectPlanStore, clock: Clock, flags: Registry): AdaptiveDefectPlanManager {
    return AdaptiveDefectPlanManager(
        store = store,
        validator = defaultAdaptiveDefectPlanValidator(),
        clock = clock,
        journal = mutableListOf<String>(),
        engine = defaultAdaptiveDefectPlanEngine(flags),
    )
}
