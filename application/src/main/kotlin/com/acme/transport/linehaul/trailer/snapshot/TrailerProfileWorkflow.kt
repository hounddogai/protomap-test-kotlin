package com.acme.transport.linehaul.trailer.snapshot

import com.acme.transport.linehaul.clock.Clock

/** Moves trailer profiles through their life cycle and keeps a journal of what it did. */
data class TrailerProfileService(
    var store: TrailerProfileStore,
    var validator: TrailerProfileValidator,
    var clock: Clock,
    var journal: MutableList<String>,
) {
    private fun note(message: String) {
        journal.add("${clock.now()} $message")
    }

    fun submit(code: String) {
        val item = store.find(code) ?: error("trailer profile $code not found")
        validator.requireValid(item)
        item.moveTo(TrailerProfileStatus.PENDING)
        store.save(item)
        note("$code is pending")
    }

    fun retire(code: String, reason: String) {
        val item = store.find(code) ?: error("trailer profile $code not found")
        if (item.isTerminal()) {
            note("$code already closed")
            return
        }
        item.moveTo(TrailerProfileStatus.CLOSED)
        store.save(item)
        note("$code is closed: $reason")
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

fun newTrailerProfileService(store: TrailerProfileStore, clock: Clock): TrailerProfileService {
    return TrailerProfileService(
        store = store,
        validator = defaultTrailerProfileValidator(),
        clock = clock,
        journal = mutableListOf<String>(),
    )
}
