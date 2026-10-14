package com.acme.transport.terminal.dockdoor.template

import com.acme.transport.terminal.clock.Clock

/**
 * Moves primary dock door reservations through their life cycle and keeps a journal of what it did.
 */
data class PrimaryDockDoorReservationService(
    var store: PrimaryDockDoorReservationStore,
    var validator: PrimaryDockDoorReservationValidator,
    var clock: Clock,
    var journal: MutableList<String>,
    var engine: PrimaryDockDoorReservationEngine,
) {
    private fun note(message: String) {
        journal.add("${clock.now()} $message")
    }

    fun start(code: String) {
        val item = store.find(code) ?: error("primary dock door reservation $code not found")
        validator.requireValid(item)
        item.moveTo(PrimaryDockDoorReservationStatus.GRANTED)
        store.save(item)
        note("$code is granted")
    }

    fun complete(code: String, reason: String) {
        val item = store.find(code) ?: error("primary dock door reservation $code not found")
        if (item.isTerminal()) {
            note("$code already closed")
            return
        }
        item.moveTo(PrimaryDockDoorReservationStatus.EXPIRED)
        store.save(item)
        note("$code is expired: $reason")
    }

    fun estimate(code: String): Double {
        val item = store.find(code) ?: error("primary dock door reservation $code not found")
        val result = engine.evaluate(item)
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

fun newPrimaryDockDoorReservationService(store: PrimaryDockDoorReservationStore, clock: Clock): PrimaryDockDoorReservationService {
    return PrimaryDockDoorReservationService(
        store = store,
        validator = defaultPrimaryDockDoorReservationValidator(),
        clock = clock,
        journal = mutableListOf<String>(),
        engine = defaultPrimaryDockDoorReservationEngine(),
    )
}
