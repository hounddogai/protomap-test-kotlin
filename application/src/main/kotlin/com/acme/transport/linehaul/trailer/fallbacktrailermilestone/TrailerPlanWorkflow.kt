package com.acme.transport.linehaul.trailer.fallbacktrailermilestone

import com.acme.transport.linehaul.clock.Clock

/** Moves trailer plans through their life cycle and keeps a journal of what it did. */
data class TrailerPlanCoordinator(
    var store: TrailerPlanStore,
    var validator: TrailerPlanValidator,
    var clock: Clock,
    var journal: MutableList<String>,
) {
    private fun note(message: String) {
        journal.add("${clock.now()} $message")
    }

    fun advance(code: String) {
        val item = store.find(code) ?: error("trailer plan $code not found")
        validator.requireValid(item)
        item.moveTo(TrailerPlanStatus.IN_REVIEW)
        store.save(item)
        note("$code is in review")
    }

    fun close(code: String, reason: String) {
        val item = store.find(code) ?: error("trailer plan $code not found")
        if (item.isTerminal()) {
            note("$code already closed")
            return
        }
        item.moveTo(TrailerPlanStatus.ARCHIVED)
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

fun newTrailerPlanCoordinator(store: TrailerPlanStore, clock: Clock): TrailerPlanCoordinator {
    return TrailerPlanCoordinator(
        store = store,
        validator = defaultTrailerPlanValidator(),
        clock = clock,
        journal = mutableListOf<String>(),
    )
}
