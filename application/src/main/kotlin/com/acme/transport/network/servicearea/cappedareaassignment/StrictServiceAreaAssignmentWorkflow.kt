package com.acme.transport.network.servicearea.cappedareaassignment

import com.acme.transport.network.clock.Clock

/**
 * Moves strict service area assignments through their life cycle and keeps a journal of what it did.
 */
data class StrictServiceAreaAssignmentCoordinator(
    var store: StrictServiceAreaAssignmentStore,
    var validator: StrictServiceAreaAssignmentValidator,
    var clock: Clock,
    var journal: MutableList<String>,
) {
    private fun note(message: String) {
        journal.add("${clock.now()} $message")
    }

    fun advance(code: String) {
        val item = store.find(code) ?: error("strict service area assignment $code not found")
        validator.requireValid(item)
        item.moveTo(StrictServiceAreaAssignmentStatus.QUEUED)
        store.save(item)
        note("$code is queued")
    }

    fun close(code: String, reason: String) {
        val item = store.find(code) ?: error("strict service area assignment $code not found")
        if (item.isTerminal()) {
            note("$code already closed")
            return
        }
        item.moveTo(StrictServiceAreaAssignmentStatus.FAILED)
        store.save(item)
        note("$code is failed: $reason")
    }

    fun scan(cutoff: Int): Int {
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

fun newStrictServiceAreaAssignmentCoordinator(store: StrictServiceAreaAssignmentStore, clock: Clock): StrictServiceAreaAssignmentCoordinator {
    return StrictServiceAreaAssignmentCoordinator(
        store = store,
        validator = defaultStrictServiceAreaAssignmentValidator(),
        clock = clock,
        journal = mutableListOf<String>(),
    )
}
