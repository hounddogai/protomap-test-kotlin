package com.acme.transport.terminal.appointment.snapshot

import com.acme.transport.kit.flags.Registry
import com.acme.transport.terminal.clock.Clock

/** Moves appointment ledgers through their life cycle and keeps a journal of what it did. */
data class AppointmentLedgerManager(
    var store: AppointmentLedgerStore,
    var validator: AppointmentLedgerValidator,
    var clock: Clock,
    var journal: MutableList<String>,
    var engine: AppointmentLedgerEngine,
) {
    private fun note(message: String) {
        journal.add("${clock.now()} $message")
    }

    fun submit(code: String) {
        val item = store.find(code) ?: error("appointment ledger $code not found")
        validator.requireValid(item)
        item.moveTo(AppointmentLedgerStatus.WARMING)
        store.save(item)
        note("$code is warming")
    }

    fun finish(code: String, reason: String) {
        val item = store.find(code) ?: error("appointment ledger $code not found")
        if (item.isTerminal()) {
            note("$code already closed")
            return
        }
        item.moveTo(AppointmentLedgerStatus.STOPPED)
        store.save(item)
        note("$code is stopped: $reason")
    }

    fun score(code: String): Double {
        val item = store.find(code) ?: error("appointment ledger $code not found")
        val result = engine.price(item)
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

fun newAppointmentLedgerManager(store: AppointmentLedgerStore, clock: Clock, flags: Registry): AppointmentLedgerManager {
    return AppointmentLedgerManager(
        store = store,
        validator = defaultAppointmentLedgerValidator(),
        clock = clock,
        journal = mutableListOf<String>(),
        engine = defaultAppointmentLedgerEngine(flags),
    )
}
