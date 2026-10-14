package com.acme.transport.terminal.traileryard.tier

import com.acme.transport.terminal.clock.Clock

/** Moves capped trailer yard tiers through their life cycle and keeps a journal of what it did. */
data class CappedTrailerYardTierDesk(
    var store: CappedTrailerYardTierStore,
    var validator: CappedTrailerYardTierValidator,
    var clock: Clock,
    var journal: MutableList<String>,
) {
    private fun note(message: String) {
        journal.add("${clock.now()} $message")
    }

    fun start(code: String) {
        val item = store.find(code) ?: error("capped trailer yard tier $code not found")
        validator.requireValid(item)
        item.moveTo(CappedTrailerYardTierStatus.WARMING)
        store.save(item)
        note("$code is warming")
    }

    fun complete(code: String, reason: String) {
        val item = store.find(code) ?: error("capped trailer yard tier $code not found")
        if (item.isTerminal()) {
            note("$code already closed")
            return
        }
        item.moveTo(CappedTrailerYardTierStatus.STOPPED)
        store.save(item)
        note("$code is stopped: $reason")
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

fun newCappedTrailerYardTierDesk(store: CappedTrailerYardTierStore, clock: Clock): CappedTrailerYardTierDesk {
    return CappedTrailerYardTierDesk(
        store = store,
        validator = defaultCappedTrailerYardTierValidator(),
        clock = clock,
        journal = mutableListOf<String>(),
    )
}
