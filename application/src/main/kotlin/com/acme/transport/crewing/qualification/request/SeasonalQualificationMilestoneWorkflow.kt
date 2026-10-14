package com.acme.transport.crewing.qualification.request

import com.acme.transport.crewing.clock.Clock
import com.acme.transport.kit.flags.Registry

/**
 * Moves seasonal qualification milestones through their life cycle and keeps a journal of what it did.
 */
data class SeasonalQualificationMilestoneDesk(
    var store: SeasonalQualificationMilestoneStore,
    var validator: SeasonalQualificationMilestoneValidator,
    var clock: Clock,
    var journal: MutableList<String>,
    var engine: SeasonalQualificationMilestoneEngine,
) {
    private fun note(message: String) {
        journal.add("${clock.now()} $message")
    }

    fun start(code: String) {
        val item = store.find(code) ?: error("seasonal qualification milestone $code not found")
        validator.requireValid(item)
        item.moveTo(SeasonalQualificationMilestoneStatus.SCHEDULED)
        store.save(item)
        note("$code is scheduled")
    }

    fun complete(code: String, reason: String) {
        val item = store.find(code) ?: error("seasonal qualification milestone $code not found")
        if (item.isTerminal()) {
            note("$code already closed")
            return
        }
        item.moveTo(SeasonalQualificationMilestoneStatus.CANCELLED)
        store.save(item)
        note("$code is cancelled: $reason")
    }

    fun estimate(code: String): Double {
        val item = store.find(code) ?: error("seasonal qualification milestone $code not found")
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

fun newSeasonalQualificationMilestoneDesk(store: SeasonalQualificationMilestoneStore, clock: Clock, flags: Registry): SeasonalQualificationMilestoneDesk {
    return SeasonalQualificationMilestoneDesk(
        store = store,
        validator = defaultSeasonalQualificationMilestoneValidator(),
        clock = clock,
        journal = mutableListOf<String>(),
        engine = defaultSeasonalQualificationMilestoneEngine(flags),
    )
}
