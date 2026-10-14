package com.acme.transport.customs.clearance.primaryclearanceoverride

import com.acme.transport.customs.clock.Clock

/** Moves strict clearance assignments through their life cycle and keeps a journal of what it did. */
data class StrictClearanceAssignmentDesk(
    var store: StrictClearanceAssignmentStore,
    var validator: StrictClearanceAssignmentValidator,
    var clock: Clock,
    var journal: MutableList<String>,
    var engine: StrictClearanceAssignmentEngine,
) {
    private fun note(message: String) {
        journal.add("${clock.now()} $message")
    }

    fun advance(code: String) {
        val item = store.find(code) ?: error("strict clearance assignment $code not found")
        validator.requireValid(item)
        item.moveTo(StrictClearanceAssignmentStatus.QUEUED)
        store.save(item)
        note("$code is queued")
    }

    fun retire(code: String, reason: String) {
        val item = store.find(code) ?: error("strict clearance assignment $code not found")
        if (item.isTerminal()) {
            note("$code already closed")
            return
        }
        item.moveTo(StrictClearanceAssignmentStatus.FAILED)
        store.save(item)
        note("$code is failed: $reason")
    }

    fun estimate(code: String): Double {
        val item = store.find(code) ?: error("strict clearance assignment $code not found")
        val result = engine.compute(item)
        note("$code evaluated at $result")
        return result
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

fun newStrictClearanceAssignmentDesk(store: StrictClearanceAssignmentStore, clock: Clock): StrictClearanceAssignmentDesk {
    return StrictClearanceAssignmentDesk(
        store = store,
        validator = defaultStrictClearanceAssignmentValidator(),
        clock = clock,
        journal = mutableListOf<String>(),
        engine = defaultStrictClearanceAssignmentEngine(),
    )
}
