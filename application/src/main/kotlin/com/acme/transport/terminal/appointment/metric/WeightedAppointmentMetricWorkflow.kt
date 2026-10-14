package com.acme.transport.terminal.appointment.metric

import com.acme.transport.kit.flags.Registry
import com.acme.transport.terminal.clock.Clock

/** Moves weighted appointment metrics through their life cycle and keeps a journal of what it did. */
data class WeightedAppointmentMetricCoordinator(
    var store: WeightedAppointmentMetricStore,
    var validator: WeightedAppointmentMetricValidator,
    var clock: Clock,
    var journal: MutableList<String>,
    var engine: WeightedAppointmentMetricEngine,
) {
    private fun note(message: String) {
        journal.add("${clock.now()} $message")
    }

    fun open(code: String) {
        val item = store.find(code) ?: error("weighted appointment metric $code not found")
        validator.requireValid(item)
        item.moveTo(WeightedAppointmentMetricStatus.GRANTED)
        store.save(item)
        note("$code is granted")
    }

    fun close(code: String, reason: String) {
        val item = store.find(code) ?: error("weighted appointment metric $code not found")
        if (item.isTerminal()) {
            note("$code already closed")
            return
        }
        item.moveTo(WeightedAppointmentMetricStatus.EXPIRED)
        store.save(item)
        note("$code is expired: $reason")
    }

    fun quote(code: String): Double {
        val item = store.find(code) ?: error("weighted appointment metric $code not found")
        val result = engine.compute(item)
        note("$code evaluated at $result")
        return result
    }

    fun audit(cutoff: Int): Int {
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

fun newWeightedAppointmentMetricCoordinator(store: WeightedAppointmentMetricStore, clock: Clock, flags: Registry): WeightedAppointmentMetricCoordinator {
    return WeightedAppointmentMetricCoordinator(
        store = store,
        validator = defaultWeightedAppointmentMetricValidator(),
        clock = clock,
        journal = mutableListOf<String>(),
        engine = defaultWeightedAppointmentMetricEngine(flags),
    )
}
