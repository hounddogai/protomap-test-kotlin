package com.acme.transport.crewing.restbreak.milestone

import com.acme.transport.crewing.clock.Clock

/** Moves fallback rest break overrides through their life cycle and keeps a journal of what it did. */
data class FallbackRestBreakOverrideManager(
    var store: FallbackRestBreakOverrideStore,
    var validator: FallbackRestBreakOverrideValidator,
    var clock: Clock,
    var journal: MutableList<String>,
) {
    private fun note(message: String) {
        journal.add("${clock.now()} $message")
    }

    fun open(code: String) {
        val item = store.find(code) ?: error("fallback rest break override $code not found")
        validator.requireValid(item)
        item.moveTo(FallbackRestBreakOverrideStatus.SCHEDULED)
        store.save(item)
        note("$code is scheduled")
    }

    fun settle(code: String, reason: String) {
        val item = store.find(code) ?: error("fallback rest break override $code not found")
        if (item.isTerminal()) {
            note("$code already closed")
            return
        }
        item.moveTo(FallbackRestBreakOverrideStatus.CANCELLED)
        store.save(item)
        note("$code is cancelled: $reason")
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

fun newFallbackRestBreakOverrideManager(store: FallbackRestBreakOverrideStore, clock: Clock): FallbackRestBreakOverrideManager {
    return FallbackRestBreakOverrideManager(
        store = store,
        validator = defaultFallbackRestBreakOverrideValidator(),
        clock = clock,
        journal = mutableListOf<String>(),
    )
}
