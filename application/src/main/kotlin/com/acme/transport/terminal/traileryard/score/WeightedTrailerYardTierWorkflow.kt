package com.acme.transport.terminal.traileryard.score

import com.acme.transport.terminal.clock.Clock

/** Moves weighted trailer yard tiers through their life cycle and keeps a journal of what it did. */
data class WeightedTrailerYardTierDesk(
    var store: WeightedTrailerYardTierStore,
    var validator: WeightedTrailerYardTierValidator,
    var clock: Clock,
    var journal: MutableList<String>,
) {
    private fun note(message: String) {
        journal.add("${clock.now()} $message")
    }

    fun submit(code: String) {
        val item = store.find(code) ?: error("weighted trailer yard tier $code not found")
        validator.requireValid(item)
        item.moveTo(WeightedTrailerYardTierStatus.IN_REVIEW)
        store.save(item)
        note("$code is in review")
    }

    fun settle(code: String, reason: String) {
        val item = store.find(code) ?: error("weighted trailer yard tier $code not found")
        if (item.isTerminal()) {
            note("$code already closed")
            return
        }
        item.moveTo(WeightedTrailerYardTierStatus.ARCHIVED)
        store.save(item)
        note("$code is archived: $reason")
    }

    fun reviewStale(cutoff: Int): Int {
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

fun newWeightedTrailerYardTierDesk(store: WeightedTrailerYardTierStore, clock: Clock): WeightedTrailerYardTierDesk {
    return WeightedTrailerYardTierDesk(
        store = store,
        validator = defaultWeightedTrailerYardTierValidator(),
        clock = clock,
        journal = mutableListOf<String>(),
    )
}
