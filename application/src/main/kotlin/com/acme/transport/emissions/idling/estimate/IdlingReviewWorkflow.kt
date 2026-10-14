package com.acme.transport.emissions.idling.estimate

import com.acme.transport.emissions.clock.Clock

/** Moves idling reviews through their life cycle and keeps a journal of what it did. */
data class IdlingReviewService(
    var store: IdlingReviewStore,
    var validator: IdlingReviewValidator,
    var clock: Clock,
    var journal: MutableList<String>,
) {
    private fun note(message: String) {
        journal.add("${clock.now()} $message")
    }

    fun advance(code: String) {
        val item = store.find(code) ?: error("idling review $code not found")
        validator.requireValid(item)
        item.moveTo(IdlingReviewStatus.VALIDATED)
        store.save(item)
        note("$code is validated")
    }

    fun close(code: String, reason: String) {
        val item = store.find(code) ?: error("idling review $code not found")
        if (item.isTerminal()) {
            note("$code already closed")
            return
        }
        item.moveTo(IdlingReviewStatus.RETIRED)
        store.save(item)
        note("$code is retired: $reason")
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

fun newIdlingReviewService(store: IdlingReviewStore, clock: Clock): IdlingReviewService {
    return IdlingReviewService(
        store = store,
        validator = defaultIdlingReviewValidator(),
        clock = clock,
        journal = mutableListOf<String>(),
    )
}
