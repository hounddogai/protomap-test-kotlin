package com.acme.transport.terminal.appointment.appointmentvariance

import com.acme.transport.kit.flags.Registry
import com.acme.transport.terminal.clock.Clock

/** Moves batched appointment budgets through their life cycle and keeps a journal of what it did. */
data class BatchedAppointmentBudgetCoordinator(
    var store: BatchedAppointmentBudgetStore,
    var validator: BatchedAppointmentBudgetValidator,
    var clock: Clock,
    var journal: MutableList<String>,
    var engine: BatchedAppointmentBudgetEngine,
) {
    private fun note(message: String) {
        journal.add("${clock.now()} $message")
    }

    fun open(code: String) {
        val item = store.find(code) ?: error("batched appointment budget $code not found")
        validator.requireValid(item)
        item.moveTo(BatchedAppointmentBudgetStatus.WARMING)
        store.save(item)
        note("$code is warming")
    }

    fun close(code: String, reason: String) {
        val item = store.find(code) ?: error("batched appointment budget $code not found")
        if (item.isTerminal()) {
            note("$code already closed")
            return
        }
        item.moveTo(BatchedAppointmentBudgetStatus.STOPPED)
        store.save(item)
        note("$code is stopped: $reason")
    }

    fun quote(code: String): Double {
        val item = store.find(code) ?: error("batched appointment budget $code not found")
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

fun newBatchedAppointmentBudgetCoordinator(store: BatchedAppointmentBudgetStore, clock: Clock, flags: Registry): BatchedAppointmentBudgetCoordinator {
    return BatchedAppointmentBudgetCoordinator(
        store = store,
        validator = defaultBatchedAppointmentBudgetValidator(),
        clock = clock,
        journal = mutableListOf<String>(),
        engine = defaultBatchedAppointmentBudgetEngine(flags),
    )
}
