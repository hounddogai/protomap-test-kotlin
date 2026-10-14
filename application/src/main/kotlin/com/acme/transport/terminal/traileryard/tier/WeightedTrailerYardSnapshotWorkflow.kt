package com.acme.transport.terminal.traileryard.tier

import com.acme.transport.terminal.clock.Clock

/**
 * Moves weighted trailer yard snapshots through their life cycle and keeps a journal of what it did.
 */
data class WeightedTrailerYardSnapshotCoordinator(
    var store: WeightedTrailerYardSnapshotStore,
    var validator: WeightedTrailerYardSnapshotValidator,
    var clock: Clock,
    var journal: MutableList<String>,
    var engine: WeightedTrailerYardSnapshotEngine,
) {
    private fun note(message: String) {
        journal.add("${clock.now()} $message")
    }

    fun advance(code: String) {
        val item = store.find(code) ?: error("weighted trailer yard snapshot $code not found")
        validator.requireValid(item)
        item.moveTo(WeightedTrailerYardSnapshotStatus.IN_REVIEW)
        store.save(item)
        note("$code is in review")
    }

    fun close(code: String, reason: String) {
        val item = store.find(code) ?: error("weighted trailer yard snapshot $code not found")
        if (item.isTerminal()) {
            note("$code already closed")
            return
        }
        item.moveTo(WeightedTrailerYardSnapshotStatus.ARCHIVED)
        store.save(item)
        note("$code is archived: $reason")
    }

    fun score(code: String): Double {
        val item = store.find(code) ?: error("weighted trailer yard snapshot $code not found")
        val result = engine.run(item)
        note("$code evaluated at $result")
        return result
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

fun newWeightedTrailerYardSnapshotCoordinator(store: WeightedTrailerYardSnapshotStore, clock: Clock): WeightedTrailerYardSnapshotCoordinator {
    return WeightedTrailerYardSnapshotCoordinator(
        store = store,
        validator = defaultWeightedTrailerYardSnapshotValidator(),
        clock = clock,
        journal = mutableListOf<String>(),
        engine = defaultWeightedTrailerYardSnapshotEngine(),
    )
}
