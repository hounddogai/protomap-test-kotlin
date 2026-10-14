package com.acme.transport.terminal.dwell.proposal

import com.acme.transport.kit.flags.Registry
import com.acme.transport.terminal.clock.Clock

/** Moves seasonal dwell policies through their life cycle and keeps a journal of what it did. */
data class SeasonalDwellPolicyManager(
    var store: SeasonalDwellPolicyStore,
    var validator: SeasonalDwellPolicyValidator,
    var clock: Clock,
    var journal: MutableList<String>,
    var engine: SeasonalDwellPolicyEngine,
) {
    private fun note(message: String) {
        journal.add("${clock.now()} $message")
    }

    fun start(code: String) {
        val item = store.find(code) ?: error("seasonal dwell policy $code not found")
        validator.requireValid(item)
        item.moveTo(SeasonalDwellPolicyStatus.PENDING)
        store.save(item)
        note("$code is pending")
    }

    fun retire(code: String, reason: String) {
        val item = store.find(code) ?: error("seasonal dwell policy $code not found")
        if (item.isTerminal()) {
            note("$code already closed")
            return
        }
        item.moveTo(SeasonalDwellPolicyStatus.CLOSED)
        store.save(item)
        note("$code is closed: $reason")
    }

    fun evaluate(code: String): Double {
        val item = store.find(code) ?: error("seasonal dwell policy $code not found")
        val result = engine.compute(item)
        note("$code evaluated at $result")
        return result
    }

    fun reviewStale(cutoff: Int): Int {
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

fun newSeasonalDwellPolicyManager(store: SeasonalDwellPolicyStore, clock: Clock, flags: Registry): SeasonalDwellPolicyManager {
    return SeasonalDwellPolicyManager(
        store = store,
        validator = defaultSeasonalDwellPolicyValidator(),
        clock = clock,
        journal = mutableListOf<String>(),
        engine = defaultSeasonalDwellPolicyEngine(flags),
    )
}
