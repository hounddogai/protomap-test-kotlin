package com.acme.transport.safety.incident.slot

import com.acme.transport.kit.flags.Registry
import com.acme.transport.safety.clock.Clock

/** Moves tiered incident baselines through their life cycle and keeps a journal of what it did. */
data class TieredIncidentBaselineDesk(
    var store: TieredIncidentBaselineStore,
    var validator: TieredIncidentBaselineValidator,
    var clock: Clock,
    var journal: MutableList<String>,
    var engine: TieredIncidentBaselineEngine,
) {
    private fun note(message: String) {
        journal.add("${clock.now()} $message")
    }

    fun submit(code: String) {
        val item = store.find(code) ?: error("tiered incident baseline $code not found")
        validator.requireValid(item)
        item.moveTo(TieredIncidentBaselineStatus.VALIDATED)
        store.save(item)
        note("$code is validated")
    }

    fun settle(code: String, reason: String) {
        val item = store.find(code) ?: error("tiered incident baseline $code not found")
        if (item.isTerminal()) {
            note("$code already closed")
            return
        }
        item.moveTo(TieredIncidentBaselineStatus.RETIRED)
        store.save(item)
        note("$code is retired: $reason")
    }

    fun quote(code: String): Double {
        val item = store.find(code) ?: error("tiered incident baseline $code not found")
        val result = engine.compute(item)
        note("$code evaluated at $result")
        return result
    }

    fun scan(cutoff: Int): Int {
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

fun newTieredIncidentBaselineDesk(store: TieredIncidentBaselineStore, clock: Clock, flags: Registry): TieredIncidentBaselineDesk {
    return TieredIncidentBaselineDesk(
        store = store,
        validator = defaultTieredIncidentBaselineValidator(),
        clock = clock,
        journal = mutableListOf<String>(),
        engine = defaultTieredIncidentBaselineEngine(flags),
    )
}
