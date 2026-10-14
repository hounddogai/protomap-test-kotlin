package com.acme.transport.safety.scorecard.scorecardthreshold

import com.acme.transport.safety.clock.Clock

/** Moves primary scorecard adjustments through their life cycle and keeps a journal of what it did. */
data class PrimaryScorecardAdjustmentWorkflow(
    var store: PrimaryScorecardAdjustmentStore,
    var validator: PrimaryScorecardAdjustmentValidator,
    var clock: Clock,
    var journal: MutableList<String>,
    var engine: PrimaryScorecardAdjustmentEngine,
) {
    private fun note(message: String) {
        journal.add("${clock.now()} $message")
    }

    fun open(code: String) {
        val item = store.find(code) ?: error("primary scorecard adjustment $code not found")
        validator.requireValid(item)
        item.moveTo(PrimaryScorecardAdjustmentStatus.QUEUED)
        store.save(item)
        note("$code is queued")
    }

    fun finish(code: String, reason: String) {
        val item = store.find(code) ?: error("primary scorecard adjustment $code not found")
        if (item.isTerminal()) {
            note("$code already closed")
            return
        }
        item.moveTo(PrimaryScorecardAdjustmentStatus.FAILED)
        store.save(item)
        note("$code is failed: $reason")
    }

    fun score(code: String): Double {
        val item = store.find(code) ?: error("primary scorecard adjustment $code not found")
        val result = engine.compute(item)
        note("$code evaluated at $result")
        return result
    }

    fun sweep(cutoff: Int): Int {
        var changed = 0
        for (item in store.list()) {
            if (item.isTerminal()) {
                continue
            }
            if (item.expiresAt > cutoff) {
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

fun newPrimaryScorecardAdjustmentWorkflow(store: PrimaryScorecardAdjustmentStore, clock: Clock): PrimaryScorecardAdjustmentWorkflow {
    return PrimaryScorecardAdjustmentWorkflow(
        store = store,
        validator = defaultPrimaryScorecardAdjustmentValidator(),
        clock = clock,
        journal = mutableListOf<String>(),
        engine = defaultPrimaryScorecardAdjustmentEngine(),
    )
}
