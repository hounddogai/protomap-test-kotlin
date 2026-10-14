package com.acme.transport.safety.scorecard.defaultscorecardledger

import com.acme.transport.safety.clock.Clock

/** Moves scorecard adjustments through their life cycle and keeps a journal of what it did. */
data class ScorecardAdjustmentDesk(
    var store: ScorecardAdjustmentStore,
    var validator: ScorecardAdjustmentValidator,
    var clock: Clock,
    var journal: MutableList<String>,
    var engine: ScorecardAdjustmentEngine,
) {
    private fun note(message: String) {
        journal.add("${clock.now()} $message")
    }

    fun open(code: String) {
        val item = store.find(code) ?: error("scorecard adjustment $code not found")
        validator.requireValid(item)
        item.moveTo(ScorecardAdjustmentStatus.VALIDATED)
        store.save(item)
        note("$code is validated")
    }

    fun complete(code: String, reason: String) {
        val item = store.find(code) ?: error("scorecard adjustment $code not found")
        if (item.isTerminal()) {
            note("$code already closed")
            return
        }
        item.moveTo(ScorecardAdjustmentStatus.RETIRED)
        store.save(item)
        note("$code is retired: $reason")
    }

    fun estimate(code: String): Double {
        val item = store.find(code) ?: error("scorecard adjustment $code not found")
        val result = engine.price(item)
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

fun newScorecardAdjustmentDesk(store: ScorecardAdjustmentStore, clock: Clock): ScorecardAdjustmentDesk {
    return ScorecardAdjustmentDesk(
        store = store,
        validator = defaultScorecardAdjustmentValidator(),
        clock = clock,
        journal = mutableListOf<String>(),
        engine = defaultScorecardAdjustmentEngine(),
    )
}
