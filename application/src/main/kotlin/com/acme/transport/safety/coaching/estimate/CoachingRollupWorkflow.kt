package com.acme.transport.safety.coaching.estimate

import com.acme.transport.kit.flags.Registry
import com.acme.transport.safety.clock.Clock

/** Moves coaching rollups through their life cycle and keeps a journal of what it did. */
data class CoachingRollupWorkflow(
    var store: CoachingRollupStore,
    var validator: CoachingRollupValidator,
    var clock: Clock,
    var journal: MutableList<String>,
    var engine: CoachingRollupEngine,
) {
    private fun note(message: String) {
        journal.add("${clock.now()} $message")
    }

    fun open(code: String) {
        val item = store.find(code) ?: error("coaching rollup $code not found")
        validator.requireValid(item)
        item.moveTo(CoachingRollupStatus.SCHEDULED)
        store.save(item)
        note("$code is scheduled")
    }

    fun settle(code: String, reason: String) {
        val item = store.find(code) ?: error("coaching rollup $code not found")
        if (item.isTerminal()) {
            note("$code already closed")
            return
        }
        item.moveTo(CoachingRollupStatus.CANCELLED)
        store.save(item)
        note("$code is cancelled: $reason")
    }

    fun quote(code: String): Double {
        val item = store.find(code) ?: error("coaching rollup $code not found")
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

fun newCoachingRollupWorkflow(store: CoachingRollupStore, clock: Clock, flags: Registry): CoachingRollupWorkflow {
    return CoachingRollupWorkflow(
        store = store,
        validator = defaultCoachingRollupValidator(),
        clock = clock,
        journal = mutableListOf<String>(),
        engine = defaultCoachingRollupEngine(flags),
    )
}
