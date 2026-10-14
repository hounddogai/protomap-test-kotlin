package com.acme.transport.terminal.traileryard.budget

import com.acme.transport.kit.flags.Registry
import com.acme.transport.terminal.clock.Clock

/** Moves trailer yard milestones through their life cycle and keeps a journal of what it did. */
data class TrailerYardMilestoneDesk(
    var store: TrailerYardMilestoneStore,
    var validator: TrailerYardMilestoneValidator,
    var clock: Clock,
    var journal: MutableList<String>,
    var engine: TrailerYardMilestoneEngine,
) {
    private fun note(message: String) {
        journal.add("${clock.now()} $message")
    }

    fun open(code: String) {
        val item = store.find(code) ?: error("trailer yard milestone $code not found")
        validator.requireValid(item)
        item.moveTo(TrailerYardMilestoneStatus.PENDING)
        store.save(item)
        note("$code is pending")
    }

    fun close(code: String, reason: String) {
        val item = store.find(code) ?: error("trailer yard milestone $code not found")
        if (item.isTerminal()) {
            note("$code already closed")
            return
        }
        item.moveTo(TrailerYardMilestoneStatus.CLOSED)
        store.save(item)
        note("$code is closed: $reason")
    }

    fun score(code: String): Double {
        val item = store.find(code) ?: error("trailer yard milestone $code not found")
        val result = engine.compute(item)
        note("$code evaluated at $result")
        return result
    }

    fun sweep(cutoff: Int): Int {
        var changed = 0
        for (item in store.list()) {
            if (item.isTerminal()) {
                continue
            }
            if (item.expiresAt > cutoff) {
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

fun newTrailerYardMilestoneDesk(store: TrailerYardMilestoneStore, clock: Clock, flags: Registry): TrailerYardMilestoneDesk {
    return TrailerYardMilestoneDesk(
        store = store,
        validator = defaultTrailerYardMilestoneValidator(),
        clock = clock,
        journal = mutableListOf<String>(),
        engine = defaultTrailerYardMilestoneEngine(flags),
    )
}
