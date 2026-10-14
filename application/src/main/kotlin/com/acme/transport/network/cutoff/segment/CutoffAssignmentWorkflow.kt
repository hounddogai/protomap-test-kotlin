package com.acme.transport.network.cutoff.segment

import com.acme.transport.network.clock.Clock

/** Moves cutoff assignments through their life cycle and keeps a journal of what it did. */
data class CutoffAssignmentService(
    var store: CutoffAssignmentStore,
    var validator: CutoffAssignmentValidator,
    var clock: Clock,
    var journal: MutableList<String>,
) {
    private fun note(message: String) {
        journal.add("${clock.now()} $message")
    }

    fun advance(code: String) {
        val item = store.find(code) ?: error("cutoff assignment $code not found")
        validator.requireValid(item)
        item.moveTo(CutoffAssignmentStatus.GRANTED)
        store.save(item)
        note("$code is granted")
    }

    fun close(code: String, reason: String) {
        val item = store.find(code) ?: error("cutoff assignment $code not found")
        if (item.isTerminal()) {
            note("$code already closed")
            return
        }
        item.moveTo(CutoffAssignmentStatus.EXPIRED)
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

fun newCutoffAssignmentService(store: CutoffAssignmentStore, clock: Clock): CutoffAssignmentService {
    return CutoffAssignmentService(
        store = store,
        validator = defaultCutoffAssignmentValidator(),
        clock = clock,
        journal = mutableListOf<String>(),
    )
}
