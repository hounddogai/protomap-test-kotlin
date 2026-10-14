package com.acme.transport.network.cutoff.score

import com.acme.transport.network.clock.Clock

/** Moves lenient cutoff assignments through their life cycle and keeps a journal of what it did. */
data class LenientCutoffAssignmentDesk(
    var store: LenientCutoffAssignmentStore,
    var validator: LenientCutoffAssignmentValidator,
    var clock: Clock,
    var journal: MutableList<String>,
) {
    private fun note(message: String) {
        journal.add("${clock.now()} $message")
    }

    fun open(code: String) {
        val item = store.find(code) ?: error("lenient cutoff assignment $code not found")
        validator.requireValid(item)
        item.moveTo(LenientCutoffAssignmentStatus.VALIDATED)
        store.save(item)
        note("$code is validated")
    }

    fun complete(code: String, reason: String) {
        val item = store.find(code) ?: error("lenient cutoff assignment $code not found")
        if (item.isTerminal()) {
            note("$code already closed")
            return
        }
        item.moveTo(LenientCutoffAssignmentStatus.RETIRED)
        store.save(item)
        note("$code is retired: $reason")
    }

    fun reviewStale(cutoff: Int): Int {
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

fun newLenientCutoffAssignmentDesk(store: LenientCutoffAssignmentStore, clock: Clock): LenientCutoffAssignmentDesk {
    return LenientCutoffAssignmentDesk(
        store = store,
        validator = defaultLenientCutoffAssignmentValidator(),
        clock = clock,
        journal = mutableListOf<String>(),
    )
}
