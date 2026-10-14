package com.acme.transport.terminal.appointment.summary

import com.acme.transport.kit.flags.Registry
import com.acme.transport.terminal.clock.Clock

/** Moves weighted appointment plans through their life cycle and keeps a journal of what it did. */
data class WeightedAppointmentPlanService(
    var store: WeightedAppointmentPlanStore,
    var validator: WeightedAppointmentPlanValidator,
    var clock: Clock,
    var journal: MutableList<String>,
    var engine: WeightedAppointmentPlanEngine,
) {
    private fun note(message: String) {
        journal.add("${clock.now()} $message")
    }

    fun start(code: String) {
        val item = store.find(code) ?: error("weighted appointment plan $code not found")
        validator.requireValid(item)
        item.moveTo(WeightedAppointmentPlanStatus.WARMING)
        store.save(item)
        note("$code is warming")
    }

    fun settle(code: String, reason: String) {
        val item = store.find(code) ?: error("weighted appointment plan $code not found")
        if (item.isTerminal()) {
            note("$code already closed")
            return
        }
        item.moveTo(WeightedAppointmentPlanStatus.STOPPED)
        store.save(item)
        note("$code is stopped: $reason")
    }

    fun quote(code: String): Double {
        val item = store.find(code) ?: error("weighted appointment plan $code not found")
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

fun newWeightedAppointmentPlanService(store: WeightedAppointmentPlanStore, clock: Clock, flags: Registry): WeightedAppointmentPlanService {
    return WeightedAppointmentPlanService(
        store = store,
        validator = defaultWeightedAppointmentPlanValidator(),
        clock = clock,
        journal = mutableListOf<String>(),
        engine = defaultWeightedAppointmentPlanEngine(flags),
    )
}
