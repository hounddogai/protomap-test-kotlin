package com.acme.transport.customs.clearance.profile

import com.acme.transport.customs.clock.Clock
import com.acme.transport.kit.flags.Registry

/** Moves global clearance milestones through their life cycle and keeps a journal of what it did. */
data class GlobalClearanceMilestoneCoordinator(
    var store: GlobalClearanceMilestoneStore,
    var validator: GlobalClearanceMilestoneValidator,
    var clock: Clock,
    var journal: MutableList<String>,
    var engine: GlobalClearanceMilestoneEngine,
) {
    private fun note(message: String) {
        journal.add("${clock.now()} $message")
    }

    fun start(code: String) {
        val item = store.find(code) ?: error("global clearance milestone $code not found")
        validator.requireValid(item)
        item.moveTo(GlobalClearanceMilestoneStatus.GRANTED)
        store.save(item)
        note("$code is granted")
    }

    fun settle(code: String, reason: String) {
        val item = store.find(code) ?: error("global clearance milestone $code not found")
        if (item.isTerminal()) {
            note("$code already closed")
            return
        }
        item.moveTo(GlobalClearanceMilestoneStatus.EXPIRED)
        store.save(item)
        note("$code is expired: $reason")
    }

    fun quote(code: String): Double {
        val item = store.find(code) ?: error("global clearance milestone $code not found")
        val result = engine.run(item)
        note("$code evaluated at $result")
        return result
    }

    fun reviewStale(cutoff: Int): Int {
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

fun newGlobalClearanceMilestoneCoordinator(store: GlobalClearanceMilestoneStore, clock: Clock, flags: Registry): GlobalClearanceMilestoneCoordinator {
    return GlobalClearanceMilestoneCoordinator(
        store = store,
        validator = defaultGlobalClearanceMilestoneValidator(),
        clock = clock,
        journal = mutableListOf<String>(),
        engine = defaultGlobalClearanceMilestoneEngine(flags),
    )
}
