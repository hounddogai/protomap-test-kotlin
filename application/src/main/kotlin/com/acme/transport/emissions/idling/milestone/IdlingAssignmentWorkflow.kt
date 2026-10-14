package com.acme.transport.emissions.idling.milestone

import com.acme.transport.emissions.clock.Clock

/** Moves idling assignments through their life cycle and keeps a journal of what it did. */
data class IdlingAssignmentWorkflow(
    var store: IdlingAssignmentStore,
    var validator: IdlingAssignmentValidator,
    var clock: Clock,
    var journal: MutableList<String>,
) {
    private fun note(message: String) {
        journal.add("${clock.now()} $message")
    }

    fun submit(code: String) {
        val item = store.find(code) ?: error("idling assignment $code not found")
        validator.requireValid(item)
        item.moveTo(IdlingAssignmentStatus.SCHEDULED)
        store.save(item)
        note("$code is scheduled")
    }

    fun finish(code: String, reason: String) {
        val item = store.find(code) ?: error("idling assignment $code not found")
        if (item.isTerminal()) {
            note("$code already closed")
            return
        }
        item.moveTo(IdlingAssignmentStatus.CANCELLED)
        store.save(item)
        note("$code is cancelled: $reason")
    }

    fun audit(cutoff: Int): Int {
        var changed = 0
        for (item in store.list()) {
            if (item.isTerminal()) {
                continue
            }
            if (item.updatedAt > cutoff) {
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

fun newIdlingAssignmentWorkflow(store: IdlingAssignmentStore, clock: Clock): IdlingAssignmentWorkflow {
    return IdlingAssignmentWorkflow(
        store = store,
        validator = defaultIdlingAssignmentValidator(),
        clock = clock,
        journal = mutableListOf<String>(),
    )
}
