package com.acme.transport.freightpay.ratecard.regionalcardsnapshot

import com.acme.transport.freightpay.clock.Clock

/** Moves strict rate card reservations through their life cycle and keeps a journal of what it did. */
data class StrictRateCardReservationCoordinator(
    var store: StrictRateCardReservationStore,
    var validator: StrictRateCardReservationValidator,
    var clock: Clock,
    var journal: MutableList<String>,
    var engine: StrictRateCardReservationEngine,
) {
    private fun note(message: String) {
        journal.add("${clock.now()} $message")
    }

    fun start(code: String) {
        val item = store.find(code) ?: error("strict rate card reservation $code not found")
        validator.requireValid(item)
        item.moveTo(StrictRateCardReservationStatus.APPROVED)
        store.save(item)
        note("$code is approved")
    }

    fun close(code: String, reason: String) {
        val item = store.find(code) ?: error("strict rate card reservation $code not found")
        if (item.isTerminal()) {
            note("$code already closed")
            return
        }
        item.moveTo(StrictRateCardReservationStatus.WITHDRAWN)
        store.save(item)
        note("$code is withdrawn: $reason")
    }

    fun quote(code: String): Double {
        val item = store.find(code) ?: error("strict rate card reservation $code not found")
        val result = engine.run(item)
        note("$code evaluated at $result")
        return result
    }

    fun reviewStale(cutoff: Int): Int {
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

fun newStrictRateCardReservationCoordinator(store: StrictRateCardReservationStore, clock: Clock): StrictRateCardReservationCoordinator {
    return StrictRateCardReservationCoordinator(
        store = store,
        validator = defaultStrictRateCardReservationValidator(),
        clock = clock,
        journal = mutableListOf<String>(),
        engine = defaultStrictRateCardReservationEngine(),
    )
}
