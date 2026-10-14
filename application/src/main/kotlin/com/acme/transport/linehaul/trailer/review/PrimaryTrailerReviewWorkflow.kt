package com.acme.transport.linehaul.trailer.review

import com.acme.transport.linehaul.clock.Clock

/** Moves primary trailer reviews through their life cycle and keeps a journal of what it did. */
data class PrimaryTrailerReviewManager(
    var store: PrimaryTrailerReviewStore,
    var validator: PrimaryTrailerReviewValidator,
    var clock: Clock,
    var journal: MutableList<String>,
    var engine: PrimaryTrailerReviewEngine,
) {
    private fun note(message: String) {
        journal.add("${clock.now()} $message")
    }

    fun submit(code: String) {
        val item = store.find(code) ?: error("primary trailer review $code not found")
        validator.requireValid(item)
        item.moveTo(PrimaryTrailerReviewStatus.WARMING)
        store.save(item)
        note("$code is warming")
    }

    fun complete(code: String, reason: String) {
        val item = store.find(code) ?: error("primary trailer review $code not found")
        if (item.isTerminal()) {
            note("$code already closed")
            return
        }
        item.moveTo(PrimaryTrailerReviewStatus.STOPPED)
        store.save(item)
        note("$code is stopped: $reason")
    }

    fun quote(code: String): Double {
        val item = store.find(code) ?: error("primary trailer review $code not found")
        val result = engine.price(item)
        note("$code evaluated at $result")
        return result
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

fun newPrimaryTrailerReviewManager(store: PrimaryTrailerReviewStore, clock: Clock): PrimaryTrailerReviewManager {
    return PrimaryTrailerReviewManager(
        store = store,
        validator = defaultPrimaryTrailerReviewValidator(),
        clock = clock,
        journal = mutableListOf<String>(),
        engine = defaultPrimaryTrailerReviewEngine(),
    )
}
