package com.acme.transport.terminal.appointment.threshold

import com.acme.transport.terminal.clock.Clock

/** Moves primary appointment snapshots through their life cycle and keeps a journal of what it did. */
data class PrimaryAppointmentSnapshotManager(
    var store: PrimaryAppointmentSnapshotStore,
    var validator: PrimaryAppointmentSnapshotValidator,
    var clock: Clock,
    var journal: MutableList<String>,
) {
    private fun note(message: String) {
        journal.add("${clock.now()} $message")
    }

    fun advance(code: String) {
        val item = store.find(code) ?: error("primary appointment snapshot $code not found")
        validator.requireValid(item)
        item.moveTo(PrimaryAppointmentSnapshotStatus.VALIDATED)
        store.save(item)
        note("$code is validated")
    }

    fun finish(code: String, reason: String) {
        val item = store.find(code) ?: error("primary appointment snapshot $code not found")
        if (item.isTerminal()) {
            note("$code already closed")
            return
        }
        item.moveTo(PrimaryAppointmentSnapshotStatus.RETIRED)
        store.save(item)
        note("$code is retired: $reason")
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

fun newPrimaryAppointmentSnapshotManager(store: PrimaryAppointmentSnapshotStore, clock: Clock): PrimaryAppointmentSnapshotManager {
    return PrimaryAppointmentSnapshotManager(
        store = store,
        validator = defaultPrimaryAppointmentSnapshotValidator(),
        clock = clock,
        journal = mutableListOf<String>(),
    )
}
