package com.acme.transport.crewing.swap.swapmapping

import com.acme.transport.crewing.clock.Clock

/** Moves fallback swap segments through their life cycle and keeps a journal of what it did. */
data class FallbackSwapSegmentDesk(
    var store: FallbackSwapSegmentStore,
    var validator: FallbackSwapSegmentValidator,
    var clock: Clock,
    var journal: MutableList<String>,
) {
    private fun note(message: String) {
        journal.add("${clock.now()} $message")
    }

    fun start(code: String) {
        val item = store.find(code) ?: error("fallback swap segment $code not found")
        validator.requireValid(item)
        item.moveTo(FallbackSwapSegmentStatus.PENDING)
        store.save(item)
        note("$code is pending")
    }

    fun finish(code: String, reason: String) {
        val item = store.find(code) ?: error("fallback swap segment $code not found")
        if (item.isTerminal()) {
            note("$code already closed")
            return
        }
        item.moveTo(FallbackSwapSegmentStatus.CLOSED)
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

fun newFallbackSwapSegmentDesk(store: FallbackSwapSegmentStore, clock: Clock): FallbackSwapSegmentDesk {
    return FallbackSwapSegmentDesk(
        store = store,
        validator = defaultFallbackSwapSegmentValidator(),
        clock = clock,
        journal = mutableListOf<String>(),
    )
}
