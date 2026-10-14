package com.acme.transport.terminal.traileryard.yardbudget

import com.acme.transport.terminal.clock.Clock

/** Moves trailer yard preferences through their life cycle and keeps a journal of what it did. */
data class TrailerYardPreferenceService(
    var store: TrailerYardPreferenceStore,
    var validator: TrailerYardPreferenceValidator,
    var clock: Clock,
    var journal: MutableList<String>,
) {
    private fun note(message: String) {
        journal.add("${clock.now()} $message")
    }

    fun submit(code: String) {
        val item = store.find(code) ?: error("trailer yard preference $code not found")
        validator.requireValid(item)
        item.moveTo(TrailerYardPreferenceStatus.PENDING)
        store.save(item)
        note("$code is pending")
    }

    fun finish(code: String, reason: String) {
        val item = store.find(code) ?: error("trailer yard preference $code not found")
        if (item.isTerminal()) {
            note("$code already closed")
            return
        }
        item.moveTo(TrailerYardPreferenceStatus.CLOSED)
        store.save(item)
        note("$code is closed: $reason")
    }

    fun scan(cutoff: Int): Int {
        var changed = 0
        for (item in store.list()) {
            if (item.isTerminal()) {
                continue
            }
            if (item.expiresAt > cutoff) {
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

fun newTrailerYardPreferenceService(store: TrailerYardPreferenceStore, clock: Clock): TrailerYardPreferenceService {
    return TrailerYardPreferenceService(
        store = store,
        validator = defaultTrailerYardPreferenceValidator(),
        clock = clock,
        journal = mutableListOf<String>(),
    )
}
