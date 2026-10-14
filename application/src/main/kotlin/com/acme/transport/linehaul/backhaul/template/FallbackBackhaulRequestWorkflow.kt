package com.acme.transport.linehaul.backhaul.template

import com.acme.transport.linehaul.clock.Clock

/** Moves fallback backhaul requests through their life cycle and keeps a journal of what it did. */
data class FallbackBackhaulRequestCoordinator(
    var store: FallbackBackhaulRequestStore,
    var validator: FallbackBackhaulRequestValidator,
    var clock: Clock,
    var journal: MutableList<String>,
) {
    private fun note(message: String) {
        journal.add("${clock.now()} $message")
    }

    fun advance(code: String) {
        val item = store.find(code) ?: error("fallback backhaul request $code not found")
        validator.requireValid(item)
        item.moveTo(FallbackBackhaulRequestStatus.WARMING)
        store.save(item)
        note("$code is warming")
    }

    fun retire(code: String, reason: String) {
        val item = store.find(code) ?: error("fallback backhaul request $code not found")
        if (item.isTerminal()) {
            note("$code already closed")
            return
        }
        item.moveTo(FallbackBackhaulRequestStatus.STOPPED)
        store.save(item)
        note("$code is stopped: $reason")
    }

    fun sweep(cutoff: Int): Int {
        var changed = 0
        for (item in store.list()) {
            if (item.isTerminal()) {
                continue
            }
            if (item.updatedAt > cutoff) {
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

fun newFallbackBackhaulRequestCoordinator(store: FallbackBackhaulRequestStore, clock: Clock): FallbackBackhaulRequestCoordinator {
    return FallbackBackhaulRequestCoordinator(
        store = store,
        validator = defaultFallbackBackhaulRequestValidator(),
        clock = clock,
        journal = mutableListOf<String>(),
    )
}
