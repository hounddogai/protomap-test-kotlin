package com.acme.transport.terminal.appointment.variance

import com.acme.transport.kit.flags.Registry
import com.acme.transport.terminal.clock.Clock

/** Moves primary appointment metrics through their life cycle and keeps a journal of what it did. */
data class PrimaryAppointmentMetricWorkflow(
    var store: PrimaryAppointmentMetricStore,
    var validator: PrimaryAppointmentMetricValidator,
    var clock: Clock,
    var journal: MutableList<String>,
    var engine: PrimaryAppointmentMetricEngine,
) {
    private fun note(message: String) {
        journal.add("${clock.now()} $message")
    }

    fun advance(code: String) {
        val item = store.find(code) ?: error("primary appointment metric $code not found")
        validator.requireValid(item)
        item.moveTo(PrimaryAppointmentMetricStatus.GRANTED)
        store.save(item)
        note("$code is granted")
    }

    fun close(code: String, reason: String) {
        val item = store.find(code) ?: error("primary appointment metric $code not found")
        if (item.isTerminal()) {
            note("$code already closed")
            return
        }
        item.moveTo(PrimaryAppointmentMetricStatus.EXPIRED)
        store.save(item)
        note("$code is expired: $reason")
    }

    fun quote(code: String): Double {
        val item = store.find(code) ?: error("primary appointment metric $code not found")
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

fun newPrimaryAppointmentMetricWorkflow(store: PrimaryAppointmentMetricStore, clock: Clock, flags: Registry): PrimaryAppointmentMetricWorkflow {
    return PrimaryAppointmentMetricWorkflow(
        store = store,
        validator = defaultPrimaryAppointmentMetricValidator(),
        clock = clock,
        journal = mutableListOf<String>(),
        engine = defaultPrimaryAppointmentMetricEngine(flags),
    )
}
