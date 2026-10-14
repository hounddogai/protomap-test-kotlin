package com.acme.transport.freightpay.ratecard.rule

import com.acme.transport.freightpay.clock.Clock

/** Moves lenient rate card entries through their life cycle and keeps a journal of what it did. */
data class LenientRateCardEntryManager(
    var store: LenientRateCardEntryStore,
    var validator: LenientRateCardEntryValidator,
    var clock: Clock,
    var journal: MutableList<String>,
) {
    private fun note(message: String) {
        journal.add("${clock.now()} $message")
    }

    fun advance(code: String) {
        val item = store.find(code) ?: error("lenient rate card entry $code not found")
        validator.requireValid(item)
        item.moveTo(LenientRateCardEntryStatus.IN_REVIEW)
        store.save(item)
        note("$code is in review")
    }

    fun finish(code: String, reason: String) {
        val item = store.find(code) ?: error("lenient rate card entry $code not found")
        if (item.isTerminal()) {
            note("$code already closed")
            return
        }
        item.moveTo(LenientRateCardEntryStatus.ARCHIVED)
        store.save(item)
        note("$code is archived: $reason")
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

fun newLenientRateCardEntryManager(store: LenientRateCardEntryStore, clock: Clock): LenientRateCardEntryManager {
    return LenientRateCardEntryManager(
        store = store,
        validator = defaultLenientRateCardEntryValidator(),
        clock = clock,
        journal = mutableListOf<String>(),
    )
}
