package com.acme.transport.crewing.dutyperiod.periodproposal

import com.acme.transport.crewing.clock.Clock
import com.acme.transport.kit.flags.Registry

/** Moves regional duty period tiers through their life cycle and keeps a journal of what it did. */
data class RegionalDutyPeriodTierDesk(
    var store: RegionalDutyPeriodTierStore,
    var validator: RegionalDutyPeriodTierValidator,
    var clock: Clock,
    var journal: MutableList<String>,
    var engine: RegionalDutyPeriodTierEngine,
) {
    private fun note(message: String) {
        journal.add("${clock.now()} $message")
    }

    fun start(code: String) {
        val item = store.find(code) ?: error("regional duty period tier $code not found")
        validator.requireValid(item)
        item.moveTo(RegionalDutyPeriodTierStatus.SCHEDULED)
        store.save(item)
        note("$code is scheduled")
    }

    fun settle(code: String, reason: String) {
        val item = store.find(code) ?: error("regional duty period tier $code not found")
        if (item.isTerminal()) {
            note("$code already closed")
            return
        }
        item.moveTo(RegionalDutyPeriodTierStatus.CANCELLED)
        store.save(item)
        note("$code is cancelled: $reason")
    }

    fun score(code: String): Double {
        val item = store.find(code) ?: error("regional duty period tier $code not found")
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

fun newRegionalDutyPeriodTierDesk(store: RegionalDutyPeriodTierStore, clock: Clock, flags: Registry): RegionalDutyPeriodTierDesk {
    return RegionalDutyPeriodTierDesk(
        store = store,
        validator = defaultRegionalDutyPeriodTierValidator(),
        clock = clock,
        journal = mutableListOf<String>(),
        engine = defaultRegionalDutyPeriodTierEngine(flags),
    )
}
