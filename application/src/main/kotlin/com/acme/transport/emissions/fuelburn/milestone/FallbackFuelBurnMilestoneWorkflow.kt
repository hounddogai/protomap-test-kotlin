package com.acme.transport.emissions.fuelburn.milestone

import com.acme.transport.emissions.clock.Clock
import com.acme.transport.kit.flags.Registry

/** Moves fallback fuel burn milestones through their life cycle and keeps a journal of what it did. */
data class FallbackFuelBurnMilestoneDesk(
    var store: FallbackFuelBurnMilestoneStore,
    var validator: FallbackFuelBurnMilestoneValidator,
    var clock: Clock,
    var journal: MutableList<String>,
    var engine: FallbackFuelBurnMilestoneEngine,
) {
    private fun note(message: String) {
        journal.add("${clock.now()} $message")
    }

    fun open(code: String) {
        val item = store.find(code) ?: error("fallback fuel burn milestone $code not found")
        validator.requireValid(item)
        item.moveTo(FallbackFuelBurnMilestoneStatus.IN_REVIEW)
        store.save(item)
        note("$code is in review")
    }

    fun retire(code: String, reason: String) {
        val item = store.find(code) ?: error("fallback fuel burn milestone $code not found")
        if (item.isTerminal()) {
            note("$code already closed")
            return
        }
        item.moveTo(FallbackFuelBurnMilestoneStatus.ARCHIVED)
        store.save(item)
        note("$code is archived: $reason")
    }

    fun evaluate(code: String): Double {
        val item = store.find(code) ?: error("fallback fuel burn milestone $code not found")
        val result = engine.run(item)
        note("$code evaluated at $result")
        return result
    }

    fun audit(cutoff: Int): Int {
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

fun newFallbackFuelBurnMilestoneDesk(store: FallbackFuelBurnMilestoneStore, clock: Clock, flags: Registry): FallbackFuelBurnMilestoneDesk {
    return FallbackFuelBurnMilestoneDesk(
        store = store,
        validator = defaultFallbackFuelBurnMilestoneValidator(),
        clock = clock,
        journal = mutableListOf<String>(),
        engine = defaultFallbackFuelBurnMilestoneEngine(flags),
    )
}
