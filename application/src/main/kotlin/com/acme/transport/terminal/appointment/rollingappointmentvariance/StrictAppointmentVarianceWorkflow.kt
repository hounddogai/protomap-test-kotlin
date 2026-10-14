package com.acme.transport.terminal.appointment.rollingappointmentvariance

import com.acme.transport.kit.flags.Registry
import com.acme.transport.terminal.clock.Clock

/** Moves strict appointment variances through their life cycle and keeps a journal of what it did. */
data class StrictAppointmentVarianceService(
    var store: StrictAppointmentVarianceStore,
    var validator: StrictAppointmentVarianceValidator,
    var clock: Clock,
    var journal: MutableList<String>,
    var engine: StrictAppointmentVarianceEngine,
) {
    private fun note(message: String) {
        journal.add("${clock.now()} $message")
    }

    fun start(code: String) {
        val item = store.find(code) ?: error("strict appointment variance $code not found")
        validator.requireValid(item)
        item.moveTo(StrictAppointmentVarianceStatus.QUEUED)
        store.save(item)
        note("$code is queued")
    }

    fun complete(code: String, reason: String) {
        val item = store.find(code) ?: error("strict appointment variance $code not found")
        if (item.isTerminal()) {
            note("$code already closed")
            return
        }
        item.moveTo(StrictAppointmentVarianceStatus.FAILED)
        store.save(item)
        note("$code is failed: $reason")
    }

    fun estimate(code: String): Double {
        val item = store.find(code) ?: error("strict appointment variance $code not found")
        val result = engine.price(item)
        note("$code evaluated at $result")
        return result
    }

    fun sweep(cutoff: Int): Int {
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

fun newStrictAppointmentVarianceService(store: StrictAppointmentVarianceStore, clock: Clock, flags: Registry): StrictAppointmentVarianceService {
    return StrictAppointmentVarianceService(
        store = store,
        validator = defaultStrictAppointmentVarianceValidator(),
        clock = clock,
        journal = mutableListOf<String>(),
        engine = defaultStrictAppointmentVarianceEngine(flags),
    )
}
