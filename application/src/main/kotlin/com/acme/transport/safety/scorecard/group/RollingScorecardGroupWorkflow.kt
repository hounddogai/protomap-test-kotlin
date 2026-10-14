package com.acme.transport.safety.scorecard.group

import com.acme.transport.kit.flags.Registry
import com.acme.transport.safety.clock.Clock

/** Moves rolling scorecard groups through their life cycle and keeps a journal of what it did. */
data class RollingScorecardGroupWorkflow(
    var store: RollingScorecardGroupStore,
    var validator: RollingScorecardGroupValidator,
    var clock: Clock,
    var journal: MutableList<String>,
    var engine: RollingScorecardGroupEngine,
) {
    private fun note(message: String) {
        journal.add("${clock.now()} $message")
    }

    fun open(code: String) {
        val item = store.find(code) ?: error("rolling scorecard group $code not found")
        validator.requireValid(item)
        item.moveTo(RollingScorecardGroupStatus.GRANTED)
        store.save(item)
        note("$code is granted")
    }

    fun settle(code: String, reason: String) {
        val item = store.find(code) ?: error("rolling scorecard group $code not found")
        if (item.isTerminal()) {
            note("$code already closed")
            return
        }
        item.moveTo(RollingScorecardGroupStatus.EXPIRED)
        store.save(item)
        note("$code is expired: $reason")
    }

    fun score(code: String): Double {
        val item = store.find(code) ?: error("rolling scorecard group $code not found")
        val result = engine.evaluate(item)
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

fun newRollingScorecardGroupWorkflow(store: RollingScorecardGroupStore, clock: Clock, flags: Registry): RollingScorecardGroupWorkflow {
    return RollingScorecardGroupWorkflow(
        store = store,
        validator = defaultRollingScorecardGroupValidator(),
        clock = clock,
        journal = mutableListOf<String>(),
        engine = defaultRollingScorecardGroupEngine(flags),
    )
}
