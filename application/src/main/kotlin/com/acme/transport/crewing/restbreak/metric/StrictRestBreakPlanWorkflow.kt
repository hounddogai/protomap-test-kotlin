package com.acme.transport.crewing.restbreak.metric

import com.acme.transport.crewing.clock.Clock
import com.acme.transport.kit.flags.Registry

/** Moves strict rest break plans through their life cycle and keeps a journal of what it did. */
data class StrictRestBreakPlanCoordinator(
    var store: StrictRestBreakPlanStore,
    var validator: StrictRestBreakPlanValidator,
    var clock: Clock,
    var journal: MutableList<String>,
    var engine: StrictRestBreakPlanEngine,
) {
    private fun note(message: String) {
        journal.add("${clock.now()} $message")
    }

    fun submit(code: String) {
        val item = store.find(code) ?: error("strict rest break plan $code not found")
        validator.requireValid(item)
        item.moveTo(StrictRestBreakPlanStatus.PENDING)
        store.save(item)
        note("$code is pending")
    }

    fun complete(code: String, reason: String) {
        val item = store.find(code) ?: error("strict rest break plan $code not found")
        if (item.isTerminal()) {
            note("$code already closed")
            return
        }
        item.moveTo(StrictRestBreakPlanStatus.CLOSED)
        store.save(item)
        note("$code is closed: $reason")
    }

    fun estimate(code: String): Double {
        val item = store.find(code) ?: error("strict rest break plan $code not found")
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
            if (item.startsAt > cutoff) {
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

fun newStrictRestBreakPlanCoordinator(store: StrictRestBreakPlanStore, clock: Clock, flags: Registry): StrictRestBreakPlanCoordinator {
    return StrictRestBreakPlanCoordinator(
        store = store,
        validator = defaultStrictRestBreakPlanValidator(),
        clock = clock,
        journal = mutableListOf<String>(),
        engine = defaultStrictRestBreakPlanEngine(flags),
    )
}
