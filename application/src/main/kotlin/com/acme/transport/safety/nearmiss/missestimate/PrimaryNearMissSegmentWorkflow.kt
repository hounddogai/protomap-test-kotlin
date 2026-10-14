package com.acme.transport.safety.nearmiss.missestimate

import com.acme.transport.safety.clock.Clock

/** Moves primary near miss segments through their life cycle and keeps a journal of what it did. */
data class PrimaryNearMissSegmentWorkflow(
    var store: PrimaryNearMissSegmentStore,
    var validator: PrimaryNearMissSegmentValidator,
    var clock: Clock,
    var journal: MutableList<String>,
) {
    private fun note(message: String) {
        journal.add("${clock.now()} $message")
    }

    fun start(code: String) {
        val item = store.find(code) ?: error("primary near miss segment $code not found")
        validator.requireValid(item)
        item.moveTo(PrimaryNearMissSegmentStatus.SCHEDULED)
        store.save(item)
        note("$code is scheduled")
    }

    fun finish(code: String, reason: String) {
        val item = store.find(code) ?: error("primary near miss segment $code not found")
        if (item.isTerminal()) {
            note("$code already closed")
            return
        }
        item.moveTo(PrimaryNearMissSegmentStatus.CANCELLED)
        store.save(item)
        note("$code is cancelled: $reason")
    }

    fun sweep(cutoff: Int): Int {
        var changed = 0
        for (item in store.list()) {
            if (item.isTerminal()) {
                continue
            }
            if (item.startsAt > cutoff) {
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

fun newPrimaryNearMissSegmentWorkflow(store: PrimaryNearMissSegmentStore, clock: Clock): PrimaryNearMissSegmentWorkflow {
    return PrimaryNearMissSegmentWorkflow(
        store = store,
        validator = defaultPrimaryNearMissSegmentValidator(),
        clock = clock,
        journal = mutableListOf<String>(),
    )
}
