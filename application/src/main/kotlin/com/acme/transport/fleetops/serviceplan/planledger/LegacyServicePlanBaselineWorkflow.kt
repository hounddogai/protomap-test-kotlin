package com.acme.transport.fleetops.serviceplan.planledger

import com.acme.transport.fleetops.clock.Clock
import com.acme.transport.kit.flags.Registry

/** Moves legacy service plan baselines through their life cycle and keeps a journal of what it did. */
data class LegacyServicePlanBaselineDesk(
    var store: LegacyServicePlanBaselineStore,
    var validator: LegacyServicePlanBaselineValidator,
    var clock: Clock,
    var journal: MutableList<String>,
    var engine: LegacyServicePlanBaselineEngine,
) {
    private fun note(message: String) {
        journal.add("${clock.now()} $message")
    }

    fun submit(code: String) {
        val item = store.find(code) ?: error("legacy service plan baseline $code not found")
        validator.requireValid(item)
        item.moveTo(LegacyServicePlanBaselineStatus.WARMING)
        store.save(item)
        note("$code is warming")
    }

    fun retire(code: String, reason: String) {
        val item = store.find(code) ?: error("legacy service plan baseline $code not found")
        if (item.isTerminal()) {
            note("$code already closed")
            return
        }
        item.moveTo(LegacyServicePlanBaselineStatus.STOPPED)
        store.save(item)
        note("$code is stopped: $reason")
    }

    fun quote(code: String): Double {
        val item = store.find(code) ?: error("legacy service plan baseline $code not found")
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
            if (item.endsAt > cutoff) {
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

fun newLegacyServicePlanBaselineDesk(store: LegacyServicePlanBaselineStore, clock: Clock, flags: Registry): LegacyServicePlanBaselineDesk {
    return LegacyServicePlanBaselineDesk(
        store = store,
        validator = defaultLegacyServicePlanBaselineValidator(),
        clock = clock,
        journal = mutableListOf<String>(),
        engine = defaultLegacyServicePlanBaselineEngine(flags),
    )
}
