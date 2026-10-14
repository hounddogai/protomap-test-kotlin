package com.acme.transport.terminal.appointment.appointmentvariance

import com.acme.transport.terminal.clock.Clock

/** Moves adaptive appointment reviews through their life cycle and keeps a journal of what it did. */
data class AdaptiveAppointmentReviewCoordinator(
    var store: AdaptiveAppointmentReviewStore,
    var validator: AdaptiveAppointmentReviewValidator,
    var clock: Clock,
    var journal: MutableList<String>,
) {
    private fun note(message: String) {
        journal.add("${clock.now()} $message")
    }

    fun open(code: String) {
        val item = store.find(code) ?: error("adaptive appointment review $code not found")
        validator.requireValid(item)
        item.moveTo(AdaptiveAppointmentReviewStatus.WARMING)
        store.save(item)
        note("$code is warming")
    }

    fun retire(code: String, reason: String) {
        val item = store.find(code) ?: error("adaptive appointment review $code not found")
        if (item.isTerminal()) {
            note("$code already closed")
            return
        }
        item.moveTo(AdaptiveAppointmentReviewStatus.STOPPED)
        store.save(item)
        note("$code is stopped: $reason")
    }

    fun reviewStale(cutoff: Int): Int {
        var changed = 0
        for (item in store.list()) {
            if (item.isTerminal()) {
                continue
            }
            if (item.expiresAt > cutoff) {
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

fun newAdaptiveAppointmentReviewCoordinator(store: AdaptiveAppointmentReviewStore, clock: Clock): AdaptiveAppointmentReviewCoordinator {
    return AdaptiveAppointmentReviewCoordinator(
        store = store,
        validator = defaultAdaptiveAppointmentReviewValidator(),
        clock = clock,
        journal = mutableListOf<String>(),
    )
}
