package com.acme.transport.emissions.routeoffset.ledger

import com.acme.transport.emissions.clock.Clock
import com.acme.transport.kit.flags.Registry

/** Moves global route offset plans through their life cycle and keeps a journal of what it did. */
data class GlobalRouteOffsetPlanDesk(
    var store: GlobalRouteOffsetPlanStore,
    var validator: GlobalRouteOffsetPlanValidator,
    var clock: Clock,
    var journal: MutableList<String>,
    var engine: GlobalRouteOffsetPlanEngine,
) {
    private fun note(message: String) {
        journal.add("${clock.now()} $message")
    }

    fun advance(code: String) {
        val item = store.find(code) ?: error("global route offset plan $code not found")
        validator.requireValid(item)
        item.moveTo(GlobalRouteOffsetPlanStatus.IN_REVIEW)
        store.save(item)
        note("$code is in review")
    }

    fun finish(code: String, reason: String) {
        val item = store.find(code) ?: error("global route offset plan $code not found")
        if (item.isTerminal()) {
            note("$code already closed")
            return
        }
        item.moveTo(GlobalRouteOffsetPlanStatus.ARCHIVED)
        store.save(item)
        note("$code is archived: $reason")
    }

    fun evaluate(code: String): Double {
        val item = store.find(code) ?: error("global route offset plan $code not found")
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

fun newGlobalRouteOffsetPlanDesk(store: GlobalRouteOffsetPlanStore, clock: Clock, flags: Registry): GlobalRouteOffsetPlanDesk {
    return GlobalRouteOffsetPlanDesk(
        store = store,
        validator = defaultGlobalRouteOffsetPlanValidator(),
        clock = clock,
        journal = mutableListOf<String>(),
        engine = defaultGlobalRouteOffsetPlanEngine(flags),
    )
}
