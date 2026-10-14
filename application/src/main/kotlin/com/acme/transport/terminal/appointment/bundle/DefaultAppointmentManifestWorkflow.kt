package com.acme.transport.terminal.appointment.bundle

import com.acme.transport.kit.flags.Registry
import com.acme.transport.terminal.clock.Clock

/** Moves default appointment manifests through their life cycle and keeps a journal of what it did. */
data class DefaultAppointmentManifestCoordinator(
    var store: DefaultAppointmentManifestStore,
    var validator: DefaultAppointmentManifestValidator,
    var clock: Clock,
    var journal: MutableList<String>,
    var engine: DefaultAppointmentManifestEngine,
) {
    private fun note(message: String) {
        journal.add("${clock.now()} $message")
    }

    fun submit(code: String) {
        val item = store.find(code) ?: error("default appointment manifest $code not found")
        validator.requireValid(item)
        item.moveTo(DefaultAppointmentManifestStatus.GRANTED)
        store.save(item)
        note("$code is granted")
    }

    fun retire(code: String, reason: String) {
        val item = store.find(code) ?: error("default appointment manifest $code not found")
        if (item.isTerminal()) {
            note("$code already closed")
            return
        }
        item.moveTo(DefaultAppointmentManifestStatus.EXPIRED)
        store.save(item)
        note("$code is expired: $reason")
    }

    fun quote(code: String): Double {
        val item = store.find(code) ?: error("default appointment manifest $code not found")
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
            if (item.updatedAt > cutoff) {
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

fun newDefaultAppointmentManifestCoordinator(store: DefaultAppointmentManifestStore, clock: Clock, flags: Registry): DefaultAppointmentManifestCoordinator {
    return DefaultAppointmentManifestCoordinator(
        store = store,
        validator = defaultDefaultAppointmentManifestValidator(),
        clock = clock,
        journal = mutableListOf<String>(),
        engine = defaultDefaultAppointmentManifestEngine(flags),
    )
}
