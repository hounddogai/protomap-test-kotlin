package com.acme.transport.lastmile.redelivery.target

import com.acme.transport.lastmile.clock.Clock

/**
 * Moves seasonal redelivery assignments through their life cycle and keeps a journal of what it did.
 */
data class SeasonalRedeliveryAssignmentCoordinator(
    var store: SeasonalRedeliveryAssignmentStore,
    var validator: SeasonalRedeliveryAssignmentValidator,
    var clock: Clock,
    var journal: MutableList<String>,
) {
    private fun note(message: String) {
        journal.add("${clock.now()} $message")
    }

    fun submit(code: String) {
        val item = store.find(code) ?: error("seasonal redelivery assignment $code not found")
        validator.requireValid(item)
        item.moveTo(SeasonalRedeliveryAssignmentStatus.VALIDATED)
        store.save(item)
        note("$code is validated")
    }

    fun finish(code: String, reason: String) {
        val item = store.find(code) ?: error("seasonal redelivery assignment $code not found")
        if (item.isTerminal()) {
            note("$code already closed")
            return
        }
        item.moveTo(SeasonalRedeliveryAssignmentStatus.RETIRED)
        store.save(item)
        note("$code is retired: $reason")
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

fun newSeasonalRedeliveryAssignmentCoordinator(store: SeasonalRedeliveryAssignmentStore, clock: Clock): SeasonalRedeliveryAssignmentCoordinator {
    return SeasonalRedeliveryAssignmentCoordinator(
        store = store,
        validator = defaultSeasonalRedeliveryAssignmentValidator(),
        clock = clock,
        journal = mutableListOf<String>(),
    )
}
