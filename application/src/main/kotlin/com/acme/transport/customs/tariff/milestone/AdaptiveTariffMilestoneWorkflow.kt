package com.acme.transport.customs.tariff.milestone

import com.acme.transport.customs.clock.Clock
import com.acme.transport.kit.flags.Registry

/** Moves adaptive tariff milestones through their life cycle and keeps a journal of what it did. */
data class AdaptiveTariffMilestoneDesk(
    var store: AdaptiveTariffMilestoneStore,
    var validator: AdaptiveTariffMilestoneValidator,
    var clock: Clock,
    var journal: MutableList<String>,
    var engine: AdaptiveTariffMilestoneEngine,
) {
    private fun note(message: String) {
        journal.add("${clock.now()} $message")
    }

    fun advance(code: String) {
        val item = store.find(code) ?: error("adaptive tariff milestone $code not found")
        validator.requireValid(item)
        item.moveTo(AdaptiveTariffMilestoneStatus.PENDING)
        store.save(item)
        note("$code is pending")
    }

    fun settle(code: String, reason: String) {
        val item = store.find(code) ?: error("adaptive tariff milestone $code not found")
        if (item.isTerminal()) {
            note("$code already closed")
            return
        }
        item.moveTo(AdaptiveTariffMilestoneStatus.CLOSED)
        store.save(item)
        note("$code is closed: $reason")
    }

    fun evaluate(code: String): Double {
        val item = store.find(code) ?: error("adaptive tariff milestone $code not found")
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

fun newAdaptiveTariffMilestoneDesk(store: AdaptiveTariffMilestoneStore, clock: Clock, flags: Registry): AdaptiveTariffMilestoneDesk {
    return AdaptiveTariffMilestoneDesk(
        store = store,
        validator = defaultAdaptiveTariffMilestoneValidator(),
        clock = clock,
        journal = mutableListOf<String>(),
        engine = defaultAdaptiveTariffMilestoneEngine(flags),
    )
}
