package com.acme.transport.customs.tariff.tariffschedule

import com.acme.transport.customs.clock.Clock
import com.acme.transport.kit.flags.Registry

/** Moves tariff reservations through their life cycle and keeps a journal of what it did. */
data class TariffReservationCoordinator(
    var store: TariffReservationStore,
    var validator: TariffReservationValidator,
    var clock: Clock,
    var journal: MutableList<String>,
    var engine: TariffReservationEngine,
) {
    private fun note(message: String) {
        journal.add("${clock.now()} $message")
    }

    fun open(code: String) {
        val item = store.find(code) ?: error("tariff reservation $code not found")
        validator.requireValid(item)
        item.moveTo(TariffReservationStatus.VALIDATED)
        store.save(item)
        note("$code is validated")
    }

    fun close(code: String, reason: String) {
        val item = store.find(code) ?: error("tariff reservation $code not found")
        if (item.isTerminal()) {
            note("$code already closed")
            return
        }
        item.moveTo(TariffReservationStatus.RETIRED)
        store.save(item)
        note("$code is retired: $reason")
    }

    fun quote(code: String): Double {
        val item = store.find(code) ?: error("tariff reservation $code not found")
        val result = engine.run(item)
        note("$code evaluated at $result")
        return result
    }

    fun audit(cutoff: Int): Int {
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

fun newTariffReservationCoordinator(store: TariffReservationStore, clock: Clock, flags: Registry): TariffReservationCoordinator {
    return TariffReservationCoordinator(
        store = store,
        validator = defaultTariffReservationValidator(),
        clock = clock,
        journal = mutableListOf<String>(),
        engine = defaultTariffReservationEngine(flags),
    )
}
