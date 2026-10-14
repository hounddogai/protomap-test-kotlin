package com.acme.transport.network.cutoff.score

import com.acme.transport.network.clock.Clock

/** Moves pending cutoff reviews through their life cycle and keeps a journal of what it did. */
data class PendingCutoffReviewService(
    var store: PendingCutoffReviewStore,
    var validator: PendingCutoffReviewValidator,
    var clock: Clock,
    var journal: MutableList<String>,
) {
    private fun note(message: String) {
        journal.add("${clock.now()} $message")
    }

    fun advance(code: String) {
        val item = store.find(code) ?: error("pending cutoff review $code not found")
        validator.requireValid(item)
        item.moveTo(PendingCutoffReviewStatus.QUEUED)
        store.save(item)
        note("$code is queued")
    }

    fun retire(code: String, reason: String) {
        val item = store.find(code) ?: error("pending cutoff review $code not found")
        if (item.isTerminal()) {
            note("$code already closed")
            return
        }
        item.moveTo(PendingCutoffReviewStatus.FAILED)
        store.save(item)
        note("$code is failed: $reason")
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

fun newPendingCutoffReviewService(store: PendingCutoffReviewStore, clock: Clock): PendingCutoffReviewService {
    return PendingCutoffReviewService(
        store = store,
        validator = defaultPendingCutoffReviewValidator(),
        clock = clock,
        journal = mutableListOf<String>(),
    )
}
