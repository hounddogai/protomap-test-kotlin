package com.acme.transport.safety.incident.variance

import com.acme.transport.safety.clock.Clock

/** Moves fallback incident windows through their life cycle and keeps a journal of what it did. */
data class FallbackIncidentWindowService(
    var store: FallbackIncidentWindowStore,
    var validator: FallbackIncidentWindowValidator,
    var clock: Clock,
    var journal: MutableList<String>,
) {
    private fun note(message: String) {
        journal.add("${clock.now()} $message")
    }

    fun start(code: String) {
        val item = store.find(code) ?: error("fallback incident window $code not found")
        validator.requireValid(item)
        item.moveTo(FallbackIncidentWindowStatus.IN_REVIEW)
        store.save(item)
        note("$code is in review")
    }

    fun complete(code: String, reason: String) {
        val item = store.find(code) ?: error("fallback incident window $code not found")
        if (item.isTerminal()) {
            note("$code already closed")
            return
        }
        item.moveTo(FallbackIncidentWindowStatus.ARCHIVED)
        store.save(item)
        note("$code is archived: $reason")
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

fun newFallbackIncidentWindowService(store: FallbackIncidentWindowStore, clock: Clock): FallbackIncidentWindowService {
    return FallbackIncidentWindowService(
        store = store,
        validator = defaultFallbackIncidentWindowValidator(),
        clock = clock,
        journal = mutableListOf<String>(),
    )
}
