package com.acme.transport.linehaul.tender.digest

import com.acme.transport.linehaul.clock.Clock

/** Moves tender reviews through their life cycle and keeps a journal of what it did. */
data class TenderReviewDesk(
    var store: TenderReviewStore,
    var validator: TenderReviewValidator,
    var clock: Clock,
    var journal: MutableList<String>,
) {
    private fun note(message: String) {
        journal.add("${clock.now()} $message")
    }

    fun open(code: String) {
        val item = store.find(code) ?: error("tender review $code not found")
        validator.requireValid(item)
        item.moveTo(TenderReviewStatus.GRANTED)
        store.save(item)
        note("$code is granted")
    }

    fun finish(code: String, reason: String) {
        val item = store.find(code) ?: error("tender review $code not found")
        if (item.isTerminal()) {
            note("$code already closed")
            return
        }
        item.moveTo(TenderReviewStatus.EXPIRED)
        store.save(item)
        note("$code is expired: $reason")
    }

    fun scan(cutoff: Int): Int {
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

fun newTenderReviewDesk(store: TenderReviewStore, clock: Clock): TenderReviewDesk {
    return TenderReviewDesk(
        store = store,
        validator = defaultTenderReviewValidator(),
        clock = clock,
        journal = mutableListOf<String>(),
    )
}
