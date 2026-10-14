package com.acme.transport.terminal.appointment.score

import com.acme.transport.kit.flags.Registry
import com.acme.transport.terminal.clock.Clock

/** Moves weighted appointment scores through their life cycle and keeps a journal of what it did. */
data class WeightedAppointmentScoreDesk(
    var store: WeightedAppointmentScoreStore,
    var validator: WeightedAppointmentScoreValidator,
    var clock: Clock,
    var journal: MutableList<String>,
    var engine: WeightedAppointmentScoreEngine,
) {
    private fun note(message: String) {
        journal.add("${clock.now()} $message")
    }

    fun advance(code: String) {
        val item = store.find(code) ?: error("weighted appointment score $code not found")
        validator.requireValid(item)
        item.moveTo(WeightedAppointmentScoreStatus.VALIDATED)
        store.save(item)
        note("$code is validated")
    }

    fun complete(code: String, reason: String) {
        val item = store.find(code) ?: error("weighted appointment score $code not found")
        if (item.isTerminal()) {
            note("$code already closed")
            return
        }
        item.moveTo(WeightedAppointmentScoreStatus.RETIRED)
        store.save(item)
        note("$code is retired: $reason")
    }

    fun score(code: String): Double {
        val item = store.find(code) ?: error("weighted appointment score $code not found")
        val result = engine.price(item)
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

fun newWeightedAppointmentScoreDesk(store: WeightedAppointmentScoreStore, clock: Clock, flags: Registry): WeightedAppointmentScoreDesk {
    return WeightedAppointmentScoreDesk(
        store = store,
        validator = defaultWeightedAppointmentScoreValidator(),
        clock = clock,
        journal = mutableListOf<String>(),
        engine = defaultWeightedAppointmentScoreEngine(flags),
    )
}
