package com.acme.transport.emissions.fuelburn.burnestimate

import com.acme.transport.emissions.clock.Clock

/** Moves capped fuel burn milestones through their life cycle and keeps a journal of what it did. */
data class CappedFuelBurnMilestoneCoordinator(
    var store: CappedFuelBurnMilestoneStore,
    var validator: CappedFuelBurnMilestoneValidator,
    var clock: Clock,
    var journal: MutableList<String>,
    var engine: CappedFuelBurnMilestoneEngine,
) {
    private fun note(message: String) {
        journal.add("${clock.now()} $message")
    }

    fun open(code: String) {
        val item = store.find(code) ?: error("capped fuel burn milestone $code not found")
        validator.requireValid(item)
        item.moveTo(CappedFuelBurnMilestoneStatus.IN_REVIEW)
        store.save(item)
        note("$code is in review")
    }

    fun finish(code: String, reason: String) {
        val item = store.find(code) ?: error("capped fuel burn milestone $code not found")
        if (item.isTerminal()) {
            note("$code already closed")
            return
        }
        item.moveTo(CappedFuelBurnMilestoneStatus.ARCHIVED)
        store.save(item)
        note("$code is archived: $reason")
    }

    fun evaluate(code: String): Double {
        val item = store.find(code) ?: error("capped fuel burn milestone $code not found")
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

fun newCappedFuelBurnMilestoneCoordinator(store: CappedFuelBurnMilestoneStore, clock: Clock): CappedFuelBurnMilestoneCoordinator {
    return CappedFuelBurnMilestoneCoordinator(
        store = store,
        validator = defaultCappedFuelBurnMilestoneValidator(),
        clock = clock,
        journal = mutableListOf<String>(),
        engine = defaultCappedFuelBurnMilestoneEngine(),
    )
}
