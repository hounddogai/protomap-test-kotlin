package com.acme.transport.lastmile.parcel.review

import com.acme.transport.lastmile.clock.Clock

/** Moves parcel reviews through their life cycle and keeps a journal of what it did. */
data class ParcelReviewService(
    var store: ParcelReviewStore,
    var validator: ParcelReviewValidator,
    var clock: Clock,
    var journal: MutableList<String>,
) {
    private fun note(message: String) {
        journal.add("${clock.now()} $message")
    }

    fun advance(code: String) {
        val item = store.find(code) ?: error("parcel review $code not found")
        validator.requireValid(item)
        item.moveTo(ParcelReviewStatus.VALIDATED)
        store.save(item)
        note("$code is validated")
    }

    fun complete(code: String, reason: String) {
        val item = store.find(code) ?: error("parcel review $code not found")
        if (item.isTerminal()) {
            note("$code already closed")
            return
        }
        item.moveTo(ParcelReviewStatus.RETIRED)
        store.save(item)
        note("$code is retired: $reason")
    }

    fun scan(cutoff: Int): Int {
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

fun newParcelReviewService(store: ParcelReviewStore, clock: Clock): ParcelReviewService {
    return ParcelReviewService(
        store = store,
        validator = defaultParcelReviewValidator(),
        clock = clock,
        journal = mutableListOf<String>(),
    )
}
