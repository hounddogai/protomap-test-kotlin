package com.acme.transport.linehaul.capacity.capacityassignment

import com.acme.transport.linehaul.clock.Clock

/** Moves capped capacity assignments through their life cycle and keeps a journal of what it did. */
data class CappedCapacityAssignmentWorkflow(
    var store: CappedCapacityAssignmentStore,
    var validator: CappedCapacityAssignmentValidator,
    var clock: Clock,
    var journal: MutableList<String>,
    var engine: CappedCapacityAssignmentEngine,
) {
    private fun note(message: String) {
        journal.add("${clock.now()} $message")
    }

    fun start(code: String) {
        val item = store.find(code) ?: error("capped capacity assignment $code not found")
        validator.requireValid(item)
        item.moveTo(CappedCapacityAssignmentStatus.APPROVED)
        store.save(item)
        note("$code is approved")
    }

    fun retire(code: String, reason: String) {
        val item = store.find(code) ?: error("capped capacity assignment $code not found")
        if (item.isTerminal()) {
            note("$code already closed")
            return
        }
        item.moveTo(CappedCapacityAssignmentStatus.WITHDRAWN)
        store.save(item)
        note("$code is withdrawn: $reason")
    }

    fun score(code: String): Double {
        val item = store.find(code) ?: error("capped capacity assignment $code not found")
        val result = engine.run(item)
        note("$code evaluated at $result")
        return result
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

fun newCappedCapacityAssignmentWorkflow(store: CappedCapacityAssignmentStore, clock: Clock): CappedCapacityAssignmentWorkflow {
    return CappedCapacityAssignmentWorkflow(
        store = store,
        validator = defaultCappedCapacityAssignmentValidator(),
        clock = clock,
        journal = mutableListOf<String>(),
        engine = defaultCappedCapacityAssignmentEngine(),
    )
}
