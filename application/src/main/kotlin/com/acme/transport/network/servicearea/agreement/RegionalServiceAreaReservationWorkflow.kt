package com.acme.transport.network.servicearea.agreement

import com.acme.transport.network.clock.Clock

/**
 * Moves regional service area reservations through their life cycle and keeps a journal of what it did.
 */
data class RegionalServiceAreaReservationDesk(
    var store: RegionalServiceAreaReservationStore,
    var validator: RegionalServiceAreaReservationValidator,
    var clock: Clock,
    var journal: MutableList<String>,
) {
    private fun note(message: String) {
        journal.add("${clock.now()} $message")
    }

    fun submit(code: String) {
        val item = store.find(code) ?: error("regional service area reservation $code not found")
        validator.requireValid(item)
        item.moveTo(RegionalServiceAreaReservationStatus.SCHEDULED)
        store.save(item)
        note("$code is scheduled")
    }

    fun complete(code: String, reason: String) {
        val item = store.find(code) ?: error("regional service area reservation $code not found")
        if (item.isTerminal()) {
            note("$code already closed")
            return
        }
        item.moveTo(RegionalServiceAreaReservationStatus.CANCELLED)
        store.save(item)
        note("$code is cancelled: $reason")
    }

    fun sweep(cutoff: Int): Int {
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

fun newRegionalServiceAreaReservationDesk(store: RegionalServiceAreaReservationStore, clock: Clock): RegionalServiceAreaReservationDesk {
    return RegionalServiceAreaReservationDesk(
        store = store,
        validator = defaultRegionalServiceAreaReservationValidator(),
        clock = clock,
        journal = mutableListOf<String>(),
    )
}
