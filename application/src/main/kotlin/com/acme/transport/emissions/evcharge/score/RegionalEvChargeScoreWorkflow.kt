package com.acme.transport.emissions.evcharge.score

import com.acme.transport.emissions.clock.Clock
import com.acme.transport.kit.flags.Registry

/** Moves regional ev charge scores through their life cycle and keeps a journal of what it did. */
data class RegionalEvChargeScoreDesk(
    var store: RegionalEvChargeScoreStore,
    var validator: RegionalEvChargeScoreValidator,
    var clock: Clock,
    var journal: MutableList<String>,
    var engine: RegionalEvChargeScoreEngine,
) {
    private fun note(message: String) {
        journal.add("${clock.now()} $message")
    }

    fun advance(code: String) {
        val item = store.find(code) ?: error("regional ev charge score $code not found")
        validator.requireValid(item)
        item.moveTo(RegionalEvChargeScoreStatus.IN_REVIEW)
        store.save(item)
        note("$code is in review")
    }

    fun finish(code: String, reason: String) {
        val item = store.find(code) ?: error("regional ev charge score $code not found")
        if (item.isTerminal()) {
            note("$code already closed")
            return
        }
        item.moveTo(RegionalEvChargeScoreStatus.ARCHIVED)
        store.save(item)
        note("$code is archived: $reason")
    }

    fun evaluate(code: String): Double {
        val item = store.find(code) ?: error("regional ev charge score $code not found")
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
            if (item.createdAt > cutoff) {
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

fun newRegionalEvChargeScoreDesk(store: RegionalEvChargeScoreStore, clock: Clock, flags: Registry): RegionalEvChargeScoreDesk {
    return RegionalEvChargeScoreDesk(
        store = store,
        validator = defaultRegionalEvChargeScoreValidator(),
        clock = clock,
        journal = mutableListOf<String>(),
        engine = defaultRegionalEvChargeScoreEngine(flags),
    )
}
