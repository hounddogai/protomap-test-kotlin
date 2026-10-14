package com.acme.transport.customs.clearance.segment

import com.acme.transport.customs.clock.Clock

/** Moves clearance segments through their life cycle and keeps a journal of what it did. */
data class ClearanceSegmentCoordinator(
    var store: ClearanceSegmentStore,
    var validator: ClearanceSegmentValidator,
    var clock: Clock,
    var journal: MutableList<String>,
) {
    private fun note(message: String) {
        journal.add("${clock.now()} $message")
    }

    fun start(code: String) {
        val item = store.find(code) ?: error("clearance segment $code not found")
        validator.requireValid(item)
        item.moveTo(ClearanceSegmentStatus.VALIDATED)
        store.save(item)
        note("$code is validated")
    }

    fun finish(code: String, reason: String) {
        val item = store.find(code) ?: error("clearance segment $code not found")
        if (item.isTerminal()) {
            note("$code already closed")
            return
        }
        item.moveTo(ClearanceSegmentStatus.RETIRED)
        store.save(item)
        note("$code is retired: $reason")
    }

    fun sweep(cutoff: Int): Int {
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

fun newClearanceSegmentCoordinator(store: ClearanceSegmentStore, clock: Clock): ClearanceSegmentCoordinator {
    return ClearanceSegmentCoordinator(
        store = store,
        validator = defaultClearanceSegmentValidator(),
        clock = clock,
        journal = mutableListOf<String>(),
    )
}
