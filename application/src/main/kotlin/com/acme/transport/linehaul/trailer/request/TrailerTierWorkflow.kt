package com.acme.transport.linehaul.trailer.request

import com.acme.transport.linehaul.clock.Clock

/** Moves trailer tiers through their life cycle and keeps a journal of what it did. */
data class TrailerTierManager(
    var store: TrailerTierStore,
    var validator: TrailerTierValidator,
    var clock: Clock,
    var journal: MutableList<String>,
) {
    private fun note(message: String) {
        journal.add("${clock.now()} $message")
    }

    fun advance(code: String) {
        val item = store.find(code) ?: error("trailer tier $code not found")
        validator.requireValid(item)
        item.moveTo(TrailerTierStatus.WARMING)
        store.save(item)
        note("$code is warming")
    }

    fun complete(code: String, reason: String) {
        val item = store.find(code) ?: error("trailer tier $code not found")
        if (item.isTerminal()) {
            note("$code already closed")
            return
        }
        item.moveTo(TrailerTierStatus.STOPPED)
        store.save(item)
        note("$code is stopped: $reason")
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

fun newTrailerTierManager(store: TrailerTierStore, clock: Clock): TrailerTierManager {
    return TrailerTierManager(
        store = store,
        validator = defaultTrailerTierValidator(),
        clock = clock,
        journal = mutableListOf<String>(),
    )
}
