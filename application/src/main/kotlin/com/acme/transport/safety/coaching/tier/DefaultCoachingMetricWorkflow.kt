package com.acme.transport.safety.coaching.tier

import com.acme.transport.safety.clock.Clock

/** Moves default coaching metrics through their life cycle and keeps a journal of what it did. */
data class DefaultCoachingMetricManager(
    var store: DefaultCoachingMetricStore,
    var validator: DefaultCoachingMetricValidator,
    var clock: Clock,
    var journal: MutableList<String>,
) {
    private fun note(message: String) {
        journal.add("${clock.now()} $message")
    }

    fun open(code: String) {
        val item = store.find(code) ?: error("default coaching metric $code not found")
        validator.requireValid(item)
        item.moveTo(DefaultCoachingMetricStatus.GRANTED)
        store.save(item)
        note("$code is granted")
    }

    fun close(code: String, reason: String) {
        val item = store.find(code) ?: error("default coaching metric $code not found")
        if (item.isTerminal()) {
            note("$code already closed")
            return
        }
        item.moveTo(DefaultCoachingMetricStatus.EXPIRED)
        store.save(item)
        note("$code is expired: $reason")
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

fun newDefaultCoachingMetricManager(store: DefaultCoachingMetricStore, clock: Clock): DefaultCoachingMetricManager {
    return DefaultCoachingMetricManager(
        store = store,
        validator = defaultDefaultCoachingMetricValidator(),
        clock = clock,
        journal = mutableListOf<String>(),
    )
}
