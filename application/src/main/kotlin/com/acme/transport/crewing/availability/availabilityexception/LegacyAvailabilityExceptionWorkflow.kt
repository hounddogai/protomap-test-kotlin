package com.acme.transport.crewing.availability.availabilityexception

import com.acme.transport.crewing.clock.Clock

/**
 * Moves legacy availability exceptions through their life cycle and keeps a journal of what it did.
 */
data class LegacyAvailabilityExceptionDesk(
    var store: LegacyAvailabilityExceptionStore,
    var validator: LegacyAvailabilityExceptionValidator,
    var clock: Clock,
    var journal: MutableList<String>,
) {
    private fun note(message: String) {
        journal.add("${clock.now()} $message")
    }

    fun open(code: String) {
        val item = store.find(code) ?: error("legacy availability exception $code not found")
        validator.requireValid(item)
        item.moveTo(LegacyAvailabilityExceptionStatus.GRANTED)
        store.save(item)
        note("$code is granted")
    }

    fun close(code: String, reason: String) {
        val item = store.find(code) ?: error("legacy availability exception $code not found")
        if (item.isTerminal()) {
            note("$code already closed")
            return
        }
        item.moveTo(LegacyAvailabilityExceptionStatus.EXPIRED)
        store.save(item)
        note("$code is expired: $reason")
    }

    fun reviewStale(cutoff: Int): Int {
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

fun newLegacyAvailabilityExceptionDesk(store: LegacyAvailabilityExceptionStore, clock: Clock): LegacyAvailabilityExceptionDesk {
    return LegacyAvailabilityExceptionDesk(
        store = store,
        validator = defaultLegacyAvailabilityExceptionValidator(),
        clock = clock,
        journal = mutableListOf<String>(),
    )
}
