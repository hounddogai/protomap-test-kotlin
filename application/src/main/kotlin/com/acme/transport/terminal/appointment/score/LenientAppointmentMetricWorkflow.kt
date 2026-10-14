package com.acme.transport.terminal.appointment.score

import com.acme.transport.terminal.clock.Clock

/** Moves lenient appointment metrics through their life cycle and keeps a journal of what it did. */
data class LenientAppointmentMetricService(
    var store: LenientAppointmentMetricStore,
    var validator: LenientAppointmentMetricValidator,
    var clock: Clock,
    var journal: MutableList<String>,
) {
    private fun note(message: String) {
        journal.add("${clock.now()} $message")
    }

    fun open(code: String) {
        val item = store.find(code) ?: error("lenient appointment metric $code not found")
        validator.requireValid(item)
        item.moveTo(LenientAppointmentMetricStatus.WARMING)
        store.save(item)
        note("$code is warming")
    }

    fun finish(code: String, reason: String) {
        val item = store.find(code) ?: error("lenient appointment metric $code not found")
        if (item.isTerminal()) {
            note("$code already closed")
            return
        }
        item.moveTo(LenientAppointmentMetricStatus.STOPPED)
        store.save(item)
        note("$code is stopped: $reason")
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

fun newLenientAppointmentMetricService(store: LenientAppointmentMetricStore, clock: Clock): LenientAppointmentMetricService {
    return LenientAppointmentMetricService(
        store = store,
        validator = defaultLenientAppointmentMetricValidator(),
        clock = clock,
        journal = mutableListOf<String>(),
    )
}
