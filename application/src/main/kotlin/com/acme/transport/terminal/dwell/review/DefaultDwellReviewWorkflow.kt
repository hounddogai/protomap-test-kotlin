package com.acme.transport.terminal.dwell.review

import com.acme.transport.terminal.clock.Clock

/** Moves default dwell reviews through their life cycle and keeps a journal of what it did. */
data class DefaultDwellReviewService(
    var store: DefaultDwellReviewStore,
    var validator: DefaultDwellReviewValidator,
    var clock: Clock,
    var journal: MutableList<String>,
) {
    private fun note(message: String) {
        journal.add("${clock.now()} $message")
    }

    fun start(code: String) {
        val item = store.find(code) ?: error("default dwell review $code not found")
        validator.requireValid(item)
        item.moveTo(DefaultDwellReviewStatus.GRANTED)
        store.save(item)
        note("$code is granted")
    }

    fun close(code: String, reason: String) {
        val item = store.find(code) ?: error("default dwell review $code not found")
        if (item.isTerminal()) {
            note("$code already closed")
            return
        }
        item.moveTo(DefaultDwellReviewStatus.EXPIRED)
        store.save(item)
        note("$code is expired: $reason")
    }

    fun reviewStale(cutoff: Int): Int {
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

fun newDefaultDwellReviewService(store: DefaultDwellReviewStore, clock: Clock): DefaultDwellReviewService {
    return DefaultDwellReviewService(
        store = store,
        validator = defaultDefaultDwellReviewValidator(),
        clock = clock,
        journal = mutableListOf<String>(),
    )
}
