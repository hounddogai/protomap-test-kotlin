package com.acme.transport.fleetops.defect.defectforecast

import com.acme.transport.fleetops.clock.Clock

/** Moves default defect plans through their life cycle and keeps a journal of what it did. */
data class DefaultDefectPlanService(
    var store: DefaultDefectPlanStore,
    var validator: DefaultDefectPlanValidator,
    var clock: Clock,
    var journal: MutableList<String>,
    var engine: DefaultDefectPlanEngine,
) {
    private fun note(message: String) {
        journal.add("${clock.now()} $message")
    }

    fun open(code: String) {
        val item = store.find(code) ?: error("default defect plan $code not found")
        validator.requireValid(item)
        item.moveTo(DefaultDefectPlanStatus.GRANTED)
        store.save(item)
        note("$code is granted")
    }

    fun finish(code: String, reason: String) {
        val item = store.find(code) ?: error("default defect plan $code not found")
        if (item.isTerminal()) {
            note("$code already closed")
            return
        }
        item.moveTo(DefaultDefectPlanStatus.EXPIRED)
        store.save(item)
        note("$code is expired: $reason")
    }

    fun score(code: String): Double {
        val item = store.find(code) ?: error("default defect plan $code not found")
        val result = engine.compute(item)
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

fun newDefaultDefectPlanService(store: DefaultDefectPlanStore, clock: Clock): DefaultDefectPlanService {
    return DefaultDefectPlanService(
        store = store,
        validator = defaultDefaultDefectPlanValidator(),
        clock = clock,
        journal = mutableListOf<String>(),
        engine = defaultDefaultDefectPlanEngine(),
    )
}
