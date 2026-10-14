package com.acme.transport.terminal.traileryard.quota

import com.acme.transport.terminal.clock.Clock

/**
 * Moves scheduled trailer yard adjustments through their life cycle and keeps a journal of what it did.
 */
data class ScheduledTrailerYardAdjustmentCoordinator(
    var store: ScheduledTrailerYardAdjustmentStore,
    var validator: ScheduledTrailerYardAdjustmentValidator,
    var clock: Clock,
    var journal: MutableList<String>,
) {
    private fun note(message: String) {
        journal.add("${clock.now()} $message")
    }

    fun submit(code: String) {
        val item = store.find(code) ?: error("scheduled trailer yard adjustment $code not found")
        validator.requireValid(item)
        item.moveTo(ScheduledTrailerYardAdjustmentStatus.VALIDATED)
        store.save(item)
        note("$code is validated")
    }

    fun finish(code: String, reason: String) {
        val item = store.find(code) ?: error("scheduled trailer yard adjustment $code not found")
        if (item.isTerminal()) {
            note("$code already closed")
            return
        }
        item.moveTo(ScheduledTrailerYardAdjustmentStatus.RETIRED)
        store.save(item)
        note("$code is retired: $reason")
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

fun newScheduledTrailerYardAdjustmentCoordinator(store: ScheduledTrailerYardAdjustmentStore, clock: Clock): ScheduledTrailerYardAdjustmentCoordinator {
    return ScheduledTrailerYardAdjustmentCoordinator(
        store = store,
        validator = defaultScheduledTrailerYardAdjustmentValidator(),
        clock = clock,
        journal = mutableListOf<String>(),
    )
}
