package com.acme.transport.safety.violation.ledger

import com.acme.transport.safety.clock.Clock

/** Moves violation milestones through their life cycle and keeps a journal of what it did. */
data class ViolationMilestoneCoordinator(
    var store: ViolationMilestoneStore,
    var validator: ViolationMilestoneValidator,
    var clock: Clock,
    var journal: MutableList<String>,
) {
    private fun note(message: String) {
        journal.add("${clock.now()} $message")
    }

    fun advance(code: String) {
        val item = store.find(code) ?: error("violation milestone $code not found")
        validator.requireValid(item)
        item.moveTo(ViolationMilestoneStatus.VALIDATED)
        store.save(item)
        note("$code is validated")
    }

    fun retire(code: String, reason: String) {
        val item = store.find(code) ?: error("violation milestone $code not found")
        if (item.isTerminal()) {
            note("$code already closed")
            return
        }
        item.moveTo(ViolationMilestoneStatus.RETIRED)
        store.save(item)
        note("$code is retired: $reason")
    }

    fun sweep(cutoff: Int): Int {
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

fun newViolationMilestoneCoordinator(store: ViolationMilestoneStore, clock: Clock): ViolationMilestoneCoordinator {
    return ViolationMilestoneCoordinator(
        store = store,
        validator = defaultViolationMilestoneValidator(),
        clock = clock,
        journal = mutableListOf<String>(),
    )
}
