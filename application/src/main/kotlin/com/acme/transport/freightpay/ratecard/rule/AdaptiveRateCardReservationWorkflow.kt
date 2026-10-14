package com.acme.transport.freightpay.ratecard.rule

import com.acme.transport.freightpay.clock.Clock
import com.acme.transport.kit.flags.Registry

/**
 * Moves adaptive rate card reservations through their life cycle and keeps a journal of what it did.
 */
data class AdaptiveRateCardReservationCoordinator(
    var store: AdaptiveRateCardReservationStore,
    var validator: AdaptiveRateCardReservationValidator,
    var clock: Clock,
    var journal: MutableList<String>,
    var engine: AdaptiveRateCardReservationEngine,
) {
    private fun note(message: String) {
        journal.add("${clock.now()} $message")
    }

    fun start(code: String) {
        val item = store.find(code) ?: error("adaptive rate card reservation $code not found")
        validator.requireValid(item)
        item.moveTo(AdaptiveRateCardReservationStatus.GRANTED)
        store.save(item)
        note("$code is granted")
    }

    fun close(code: String, reason: String) {
        val item = store.find(code) ?: error("adaptive rate card reservation $code not found")
        if (item.isTerminal()) {
            note("$code already closed")
            return
        }
        item.moveTo(AdaptiveRateCardReservationStatus.EXPIRED)
        store.save(item)
        note("$code is expired: $reason")
    }

    fun quote(code: String): Double {
        val item = store.find(code) ?: error("adaptive rate card reservation $code not found")
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

fun newAdaptiveRateCardReservationCoordinator(store: AdaptiveRateCardReservationStore, clock: Clock, flags: Registry): AdaptiveRateCardReservationCoordinator {
    return AdaptiveRateCardReservationCoordinator(
        store = store,
        validator = defaultAdaptiveRateCardReservationValidator(),
        clock = clock,
        journal = mutableListOf<String>(),
        engine = defaultAdaptiveRateCardReservationEngine(flags),
    )
}
