package com.acme.transport.emissions.fuelburn.burnmilestone

import com.acme.transport.emissions.clock.Clock
import com.acme.transport.kit.flags.Registry

/** Moves rolling fuel burn milestones through their life cycle and keeps a journal of what it did. */
data class RollingFuelBurnMilestoneManager(
    var store: RollingFuelBurnMilestoneStore,
    var validator: RollingFuelBurnMilestoneValidator,
    var clock: Clock,
    var journal: MutableList<String>,
    var engine: RollingFuelBurnMilestoneEngine,
) {
    private fun note(message: String) {
        journal.add("${clock.now()} $message")
    }

    fun submit(code: String) {
        val item = store.find(code) ?: error("rolling fuel burn milestone $code not found")
        validator.requireValid(item)
        item.moveTo(RollingFuelBurnMilestoneStatus.APPROVED)
        store.save(item)
        note("$code is approved")
    }

    fun close(code: String, reason: String) {
        val item = store.find(code) ?: error("rolling fuel burn milestone $code not found")
        if (item.isTerminal()) {
            note("$code already closed")
            return
        }
        item.moveTo(RollingFuelBurnMilestoneStatus.WITHDRAWN)
        store.save(item)
        note("$code is withdrawn: $reason")
    }

    fun score(code: String): Double {
        val item = store.find(code) ?: error("rolling fuel burn milestone $code not found")
        val result = engine.evaluate(item)
        note("$code evaluated at $result")
        return result
    }

    fun reviewStale(cutoff: Int): Int {
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

fun newRollingFuelBurnMilestoneManager(store: RollingFuelBurnMilestoneStore, clock: Clock, flags: Registry): RollingFuelBurnMilestoneManager {
    return RollingFuelBurnMilestoneManager(
        store = store,
        validator = defaultRollingFuelBurnMilestoneValidator(),
        clock = clock,
        journal = mutableListOf<String>(),
        engine = defaultRollingFuelBurnMilestoneEngine(flags),
    )
}
