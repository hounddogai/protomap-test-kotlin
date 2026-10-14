package com.acme.transport.terminal.dwell.milestone

import com.acme.transport.kit.flags.Registry
import com.acme.transport.terminal.clock.Clock

/** Moves lenient dwell milestones through their life cycle and keeps a journal of what it did. */
data class LenientDwellMilestoneDesk(
    var store: LenientDwellMilestoneStore,
    var validator: LenientDwellMilestoneValidator,
    var clock: Clock,
    var journal: MutableList<String>,
    var engine: LenientDwellMilestoneEngine,
) {
    private fun note(message: String) {
        journal.add("${clock.now()} $message")
    }

    fun advance(code: String) {
        val item = store.find(code) ?: error("lenient dwell milestone $code not found")
        validator.requireValid(item)
        item.moveTo(LenientDwellMilestoneStatus.QUEUED)
        store.save(item)
        note("$code is queued")
    }

    fun finish(code: String, reason: String) {
        val item = store.find(code) ?: error("lenient dwell milestone $code not found")
        if (item.isTerminal()) {
            note("$code already closed")
            return
        }
        item.moveTo(LenientDwellMilestoneStatus.FAILED)
        store.save(item)
        note("$code is failed: $reason")
    }

    fun quote(code: String): Double {
        val item = store.find(code) ?: error("lenient dwell milestone $code not found")
        val result = engine.price(item)
        note("$code evaluated at $result")
        return result
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

fun newLenientDwellMilestoneDesk(store: LenientDwellMilestoneStore, clock: Clock, flags: Registry): LenientDwellMilestoneDesk {
    return LenientDwellMilestoneDesk(
        store = store,
        validator = defaultLenientDwellMilestoneValidator(),
        clock = clock,
        journal = mutableListOf<String>(),
        engine = defaultLenientDwellMilestoneEngine(flags),
    )
}
