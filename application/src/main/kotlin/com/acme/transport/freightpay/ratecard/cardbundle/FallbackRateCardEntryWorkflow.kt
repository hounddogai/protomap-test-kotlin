package com.acme.transport.freightpay.ratecard.cardbundle

import com.acme.transport.freightpay.clock.Clock

/** Moves fallback rate card entries through their life cycle and keeps a journal of what it did. */
data class FallbackRateCardEntryDesk(
    var store: FallbackRateCardEntryStore,
    var validator: FallbackRateCardEntryValidator,
    var clock: Clock,
    var journal: MutableList<String>,
) {
    private fun note(message: String) {
        journal.add("${clock.now()} $message")
    }

    fun start(code: String) {
        val item = store.find(code) ?: error("fallback rate card entry $code not found")
        validator.requireValid(item)
        item.moveTo(FallbackRateCardEntryStatus.PENDING)
        store.save(item)
        note("$code is pending")
    }

    fun settle(code: String, reason: String) {
        val item = store.find(code) ?: error("fallback rate card entry $code not found")
        if (item.isTerminal()) {
            note("$code already closed")
            return
        }
        item.moveTo(FallbackRateCardEntryStatus.CLOSED)
        store.save(item)
        note("$code is closed: $reason")
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

fun newFallbackRateCardEntryDesk(store: FallbackRateCardEntryStore, clock: Clock): FallbackRateCardEntryDesk {
    return FallbackRateCardEntryDesk(
        store = store,
        validator = defaultFallbackRateCardEntryValidator(),
        clock = clock,
        journal = mutableListOf<String>(),
    )
}
