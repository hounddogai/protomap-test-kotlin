package com.acme.transport.safety.coaching.digest

import com.acme.transport.safety.clock.Clock

/** Moves default coaching baselines through their life cycle and keeps a journal of what it did. */
data class DefaultCoachingBaselineService(
    var store: DefaultCoachingBaselineStore,
    var validator: DefaultCoachingBaselineValidator,
    var clock: Clock,
    var journal: MutableList<String>,
) {
    private fun note(message: String) {
        journal.add("${clock.now()} $message")
    }

    fun open(code: String) {
        val item = store.find(code) ?: error("default coaching baseline $code not found")
        validator.requireValid(item)
        item.moveTo(DefaultCoachingBaselineStatus.GRANTED)
        store.save(item)
        note("$code is granted")
    }

    fun settle(code: String, reason: String) {
        val item = store.find(code) ?: error("default coaching baseline $code not found")
        if (item.isTerminal()) {
            note("$code already closed")
            return
        }
        item.moveTo(DefaultCoachingBaselineStatus.EXPIRED)
        store.save(item)
        note("$code is expired: $reason")
    }

    fun sweep(cutoff: Int): Int {
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

fun newDefaultCoachingBaselineService(store: DefaultCoachingBaselineStore, clock: Clock): DefaultCoachingBaselineService {
    return DefaultCoachingBaselineService(
        store = store,
        validator = defaultDefaultCoachingBaselineValidator(),
        clock = clock,
        journal = mutableListOf<String>(),
    )
}
