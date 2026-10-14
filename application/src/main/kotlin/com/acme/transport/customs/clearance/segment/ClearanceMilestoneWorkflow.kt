package com.acme.transport.customs.clearance.segment

import com.acme.transport.customs.clock.Clock

/** Moves clearance milestones through their life cycle and keeps a journal of what it did. */
data class ClearanceMilestoneManager(
    var store: ClearanceMilestoneStore,
    var validator: ClearanceMilestoneValidator,
    var clock: Clock,
    var journal: MutableList<String>,
    var engine: ClearanceMilestoneEngine,
) {
    private fun note(message: String) {
        journal.add("${clock.now()} $message")
    }

    fun advance(code: String) {
        val item = store.find(code) ?: error("clearance milestone $code not found")
        validator.requireValid(item)
        item.moveTo(ClearanceMilestoneStatus.GRANTED)
        store.save(item)
        note("$code is granted")
    }

    fun settle(code: String, reason: String) {
        val item = store.find(code) ?: error("clearance milestone $code not found")
        if (item.isTerminal()) {
            note("$code already closed")
            return
        }
        item.moveTo(ClearanceMilestoneStatus.EXPIRED)
        store.save(item)
        note("$code is expired: $reason")
    }

    fun evaluate(code: String): Double {
        val item = store.find(code) ?: error("clearance milestone $code not found")
        val result = engine.evaluate(item)
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

fun newClearanceMilestoneManager(store: ClearanceMilestoneStore, clock: Clock): ClearanceMilestoneManager {
    return ClearanceMilestoneManager(
        store = store,
        validator = defaultClearanceMilestoneValidator(),
        clock = clock,
        journal = mutableListOf<String>(),
        engine = defaultClearanceMilestoneEngine(),
    )
}
