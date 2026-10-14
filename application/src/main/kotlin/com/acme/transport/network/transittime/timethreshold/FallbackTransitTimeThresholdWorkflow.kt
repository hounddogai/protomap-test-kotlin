package com.acme.transport.network.transittime.timethreshold

import com.acme.transport.network.clock.Clock

/**
 * Moves fallback transit time thresholds through their life cycle and keeps a journal of what it did.
 */
data class FallbackTransitTimeThresholdDesk(
    var store: FallbackTransitTimeThresholdStore,
    var validator: FallbackTransitTimeThresholdValidator,
    var clock: Clock,
    var journal: MutableList<String>,
) {
    private fun note(message: String) {
        journal.add("${clock.now()} $message")
    }

    fun start(code: String) {
        val item = store.find(code) ?: error("fallback transit time threshold $code not found")
        validator.requireValid(item)
        item.moveTo(FallbackTransitTimeThresholdStatus.IN_REVIEW)
        store.save(item)
        note("$code is in review")
    }

    fun complete(code: String, reason: String) {
        val item = store.find(code) ?: error("fallback transit time threshold $code not found")
        if (item.isTerminal()) {
            note("$code already closed")
            return
        }
        item.moveTo(FallbackTransitTimeThresholdStatus.ARCHIVED)
        store.save(item)
        note("$code is archived: $reason")
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

fun newFallbackTransitTimeThresholdDesk(store: FallbackTransitTimeThresholdStore, clock: Clock): FallbackTransitTimeThresholdDesk {
    return FallbackTransitTimeThresholdDesk(
        store = store,
        validator = defaultFallbackTransitTimeThresholdValidator(),
        clock = clock,
        journal = mutableListOf<String>(),
    )
}
