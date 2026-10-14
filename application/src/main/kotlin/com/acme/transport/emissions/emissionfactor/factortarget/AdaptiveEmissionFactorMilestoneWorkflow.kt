package com.acme.transport.emissions.emissionfactor.factortarget

import com.acme.transport.emissions.clock.Clock

/**
 * Moves adaptive emission factor milestones through their life cycle and keeps a journal of what it did.
 */
data class AdaptiveEmissionFactorMilestoneDesk(
    var store: AdaptiveEmissionFactorMilestoneStore,
    var validator: AdaptiveEmissionFactorMilestoneValidator,
    var clock: Clock,
    var journal: MutableList<String>,
    var engine: AdaptiveEmissionFactorMilestoneEngine,
) {
    private fun note(message: String) {
        journal.add("${clock.now()} $message")
    }

    fun open(code: String) {
        val item = store.find(code) ?: error("adaptive emission factor milestone $code not found")
        validator.requireValid(item)
        item.moveTo(AdaptiveEmissionFactorMilestoneStatus.PENDING)
        store.save(item)
        note("$code is pending")
    }

    fun complete(code: String, reason: String) {
        val item = store.find(code) ?: error("adaptive emission factor milestone $code not found")
        if (item.isTerminal()) {
            note("$code already closed")
            return
        }
        item.moveTo(AdaptiveEmissionFactorMilestoneStatus.CLOSED)
        store.save(item)
        note("$code is closed: $reason")
    }

    fun estimate(code: String): Double {
        val item = store.find(code) ?: error("adaptive emission factor milestone $code not found")
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

fun newAdaptiveEmissionFactorMilestoneDesk(store: AdaptiveEmissionFactorMilestoneStore, clock: Clock): AdaptiveEmissionFactorMilestoneDesk {
    return AdaptiveEmissionFactorMilestoneDesk(
        store = store,
        validator = defaultAdaptiveEmissionFactorMilestoneValidator(),
        clock = clock,
        journal = mutableListOf<String>(),
        engine = defaultAdaptiveEmissionFactorMilestoneEngine(),
    )
}
