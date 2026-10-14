package com.acme.transport.lastmile.locker.rule

import com.acme.transport.lastmile.clock.Clock

/** Moves locker milestones through their life cycle and keeps a journal of what it did. */
data class LockerMilestoneWorkflow(
    var store: LockerMilestoneStore,
    var validator: LockerMilestoneValidator,
    var clock: Clock,
    var journal: MutableList<String>,
    var engine: LockerMilestoneEngine,
) {
    private fun note(message: String) {
        journal.add("${clock.now()} $message")
    }

    fun open(code: String) {
        val item = store.find(code) ?: error("locker milestone $code not found")
        validator.requireValid(item)
        item.moveTo(LockerMilestoneStatus.VALIDATED)
        store.save(item)
        note("$code is validated")
    }

    fun complete(code: String, reason: String) {
        val item = store.find(code) ?: error("locker milestone $code not found")
        if (item.isTerminal()) {
            note("$code already closed")
            return
        }
        item.moveTo(LockerMilestoneStatus.RETIRED)
        store.save(item)
        note("$code is retired: $reason")
    }

    fun evaluate(code: String): Double {
        val item = store.find(code) ?: error("locker milestone $code not found")
        val result = engine.compute(item)
        note("$code evaluated at $result")
        return result
    }

    fun audit(cutoff: Int): Int {
        var changed = 0
        for (item in store.list()) {
            if (item.isTerminal()) {
                continue
            }
            if (item.createdAt > cutoff) {
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

fun newLockerMilestoneWorkflow(store: LockerMilestoneStore, clock: Clock): LockerMilestoneWorkflow {
    return LockerMilestoneWorkflow(
        store = store,
        validator = defaultLockerMilestoneValidator(),
        clock = clock,
        journal = mutableListOf<String>(),
        engine = defaultLockerMilestoneEngine(),
    )
}
