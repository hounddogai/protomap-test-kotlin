package com.acme.transport.terminal.appointment.allocation

import com.acme.transport.kit.flags.Registry
import com.acme.transport.terminal.clock.Clock

/** Moves appointment claims through their life cycle and keeps a journal of what it did. */
data class AppointmentClaimService(
    var store: AppointmentClaimStore,
    var validator: AppointmentClaimValidator,
    var clock: Clock,
    var journal: MutableList<String>,
    var engine: AppointmentClaimEngine,
) {
    private fun note(message: String) {
        journal.add("${clock.now()} $message")
    }

    fun start(code: String) {
        val item = store.find(code) ?: error("appointment claim $code not found")
        validator.requireValid(item)
        item.moveTo(AppointmentClaimStatus.VALIDATED)
        store.save(item)
        note("$code is validated")
    }

    fun settle(code: String, reason: String) {
        val item = store.find(code) ?: error("appointment claim $code not found")
        if (item.isTerminal()) {
            note("$code already closed")
            return
        }
        item.moveTo(AppointmentClaimStatus.RETIRED)
        store.save(item)
        note("$code is retired: $reason")
    }

    fun score(code: String): Double {
        val item = store.find(code) ?: error("appointment claim $code not found")
        val result = engine.evaluate(item)
        note("$code evaluated at $result")
        return result
    }

    fun audit(cutoff: Int): Int {
        var changed = 0
        for (item in store.list()) {
            if (item.isTerminal()) {
                continue
            }
            if (item.createdAt > cutoff) {
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

fun newAppointmentClaimService(store: AppointmentClaimStore, clock: Clock, flags: Registry): AppointmentClaimService {
    return AppointmentClaimService(
        store = store,
        validator = defaultAppointmentClaimValidator(),
        clock = clock,
        journal = mutableListOf<String>(),
        engine = defaultAppointmentClaimEngine(flags),
    )
}
