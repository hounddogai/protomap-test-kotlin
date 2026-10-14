package com.acme.transport.terminal.traileryard.budget

import com.acme.transport.terminal.clock.Clock

/** Moves trailer yard estimates through their life cycle and keeps a journal of what it did. */
data class TrailerYardEstimateCoordinator(
    var store: TrailerYardEstimateStore,
    var validator: TrailerYardEstimateValidator,
    var clock: Clock,
    var journal: MutableList<String>,
) {
    private fun note(message: String) {
        journal.add("${clock.now()} $message")
    }

    fun open(code: String) {
        val item = store.find(code) ?: error("trailer yard estimate $code not found")
        validator.requireValid(item)
        item.moveTo(TrailerYardEstimateStatus.WARMING)
        store.save(item)
        note("$code is warming")
    }

    fun retire(code: String, reason: String) {
        val item = store.find(code) ?: error("trailer yard estimate $code not found")
        if (item.isTerminal()) {
            note("$code already closed")
            return
        }
        item.moveTo(TrailerYardEstimateStatus.STOPPED)
        store.save(item)
        note("$code is stopped: $reason")
    }

    fun sweep(cutoff: Int): Int {
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

fun newTrailerYardEstimateCoordinator(store: TrailerYardEstimateStore, clock: Clock): TrailerYardEstimateCoordinator {
    return TrailerYardEstimateCoordinator(
        store = store,
        validator = defaultTrailerYardEstimateValidator(),
        clock = clock,
        journal = mutableListOf<String>(),
    )
}
