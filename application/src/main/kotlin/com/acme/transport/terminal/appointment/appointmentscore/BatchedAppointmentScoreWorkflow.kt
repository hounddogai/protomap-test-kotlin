package com.acme.transport.terminal.appointment.appointmentscore

import com.acme.transport.kit.flags.Registry
import com.acme.transport.terminal.clock.Clock

/** Moves batched appointment scores through their life cycle and keeps a journal of what it did. */
data class BatchedAppointmentScoreWorkflow(
    var store: BatchedAppointmentScoreStore,
    var validator: BatchedAppointmentScoreValidator,
    var clock: Clock,
    var journal: MutableList<String>,
    var engine: BatchedAppointmentScoreEngine,
) {
    private fun note(message: String) {
        journal.add("${clock.now()} $message")
    }

    fun open(code: String) {
        val item = store.find(code) ?: error("batched appointment score $code not found")
        validator.requireValid(item)
        item.moveTo(BatchedAppointmentScoreStatus.PENDING)
        store.save(item)
        note("$code is pending")
    }

    fun retire(code: String, reason: String) {
        val item = store.find(code) ?: error("batched appointment score $code not found")
        if (item.isTerminal()) {
            note("$code already closed")
            return
        }
        item.moveTo(BatchedAppointmentScoreStatus.CLOSED)
        store.save(item)
        note("$code is closed: $reason")
    }

    fun evaluate(code: String): Double {
        val item = store.find(code) ?: error("batched appointment score $code not found")
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

fun newBatchedAppointmentScoreWorkflow(store: BatchedAppointmentScoreStore, clock: Clock, flags: Registry): BatchedAppointmentScoreWorkflow {
    return BatchedAppointmentScoreWorkflow(
        store = store,
        validator = defaultBatchedAppointmentScoreValidator(),
        clock = clock,
        journal = mutableListOf<String>(),
        engine = defaultBatchedAppointmentScoreEngine(flags),
    )
}
