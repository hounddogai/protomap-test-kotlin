package com.acme.transport.linehaul.backhaul.milestone

import com.acme.transport.kit.flags.Registry
import com.acme.transport.linehaul.clock.Clock

/** Moves backhaul milestones through their life cycle and keeps a journal of what it did. */
data class BackhaulMilestoneManager(
    var store: BackhaulMilestoneStore,
    var validator: BackhaulMilestoneValidator,
    var clock: Clock,
    var journal: MutableList<String>,
    var engine: BackhaulMilestoneEngine,
) {
    private fun note(message: String) {
        journal.add("${clock.now()} $message")
    }

    fun submit(code: String) {
        val item = store.find(code) ?: error("backhaul milestone $code not found")
        validator.requireValid(item)
        item.moveTo(BackhaulMilestoneStatus.IN_REVIEW)
        store.save(item)
        note("$code is in review")
    }

    fun settle(code: String, reason: String) {
        val item = store.find(code) ?: error("backhaul milestone $code not found")
        if (item.isTerminal()) {
            note("$code already closed")
            return
        }
        item.moveTo(BackhaulMilestoneStatus.ARCHIVED)
        store.save(item)
        note("$code is archived: $reason")
    }

    fun evaluate(code: String): Double {
        val item = store.find(code) ?: error("backhaul milestone $code not found")
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
            if (item.endsAt > cutoff) {
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

fun newBackhaulMilestoneManager(store: BackhaulMilestoneStore, clock: Clock, flags: Registry): BackhaulMilestoneManager {
    return BackhaulMilestoneManager(
        store = store,
        validator = defaultBackhaulMilestoneValidator(),
        clock = clock,
        journal = mutableListOf<String>(),
        engine = defaultBackhaulMilestoneEngine(flags),
    )
}
